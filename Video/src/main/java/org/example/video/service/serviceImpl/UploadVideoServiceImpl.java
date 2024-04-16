package org.example.video.service.serviceImpl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.example.Model.annotation.GetFilter;
import org.example.Model.constant.LikeNotifyConst;
import org.example.Model.constant.ParentType;
import org.example.Model.entity.*;
import org.example.Model.pojo.*;
import org.example.Model.pojo.Time;
import org.example.video.Model.pojo.Result;
import org.example.video.async.VideoAsyncHandler;
import org.example.video.config.MinioConfigProperties;
import org.example.video.config.RedisExpireTime;
import org.example.video.constant.FileStorageType;
import org.example.video.constant.RabbitMQConst;
import org.example.video.constant.RedisPrefix;
import org.example.video.entity.*;
import org.example.video.exception.FileExistsException;
import org.example.video.mapper.*;
import org.example.video.pojo.*;
import org.example.video.pojo.VideoInfo;
import org.example.video.service.UploadVideoService;
import org.example.video.utils.JedisUtil;
import org.example.video.utils.ThreadUtils;
import org.example.video.utils.UserInfoClient;
import org.example.video.utils.VideoUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import redis.clients.jedis.Jedis;
import ws.schild.jave.*;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class UploadVideoServiceImpl implements UploadVideoService {
    @Autowired
    private MinioClient client;
    @Value("${upload.localpath}")
    private String localPath;
    @Value("${minio.bucket}")
    private String bucket;
    @Value("${minio.imgbucket}")
    private String imgBucket;
    @Autowired
    private MinioConfigProperties minioConfigProperties;
    @Autowired
    private FileBelongMapper fileBelongMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private MinioConfigProperties configProperties;
    @Autowired
    private FileStorageMapper fileStorageMapper;
    @Autowired
    private VideoMapper videoMapper;
    @Autowired
    private VideoItemMapper videoItemMapper;
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private UserInfoClient infoClient;
    @Autowired
    private LikeRecordMapper likeRecordMapper;
    @Autowired
    private CoinsRecordMapper coinsRecordMapper;
    @Autowired
    private LikeNotifyMapper notifyMapper;
    @Autowired
    private CoinsAddRecordMapper addRecordMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private EverydayPlaysMapper everydayPlaysMapper;
    @Autowired
    private DanmuMapper danmuMapper;
    @Autowired
    private VideoAsyncHandler handler;
    @Autowired
    private RedissonClient redissonClient;

    @Override
    @Transactional
    public Result<VideoChunkVO> uploadChunk(MultipartFile file, int chunkIndex, int chunkTotal, String name,String extension){
        //检测是否为当前用户正在上传的文件，如果不是，返回失败
        Integer usedId = ThreadUtils.get();
        if(name!=null&&!checkExist(name, usedId)){
            log.info("用户已经在上传其他文件，一次只能上传一种文件");
            //返回现在的文件名和进度
            VideoChunkVO t = new VideoChunkVO();
            Set<String> keys = JedisUtil.getJedis().keys(usedId + ":*");
            for (String key : keys) {
                t.setFilename(handleFilenameInRedis(key));
                String s = JedisUtil.getJedis().get(key);
                String[] split = s.split("/");
                Integer nowIndex=Integer.parseInt(split[0]);
                Integer total=Integer.parseInt(split[1]);
                t.setProcessIndex(nowIndex);    //返回目前进度
                t.setTotalProcess(total);       //和总进度
                t.setType(1); //代表文件无效
                break;
            }
            return Result.success(t);   //上传错误，返回现在上传的文件名
        }
        //检测chunkTotal是否有问题
        //检测chunkIndex是否有问题
        Result<VideoChunkVO> result = getChunkTotal(name, usedId, chunkTotal, chunkIndex);
        if(result!=null){
            return result;
        }
        int userId= ThreadUtils.get();
        if(chunkTotal<=0){
            return Result.fail("分块总数为负数或0");
        }
        if(chunkIndex<0||chunkIndex>=chunkTotal){
            return Result.fail("分块序号有问题");
        }

        if(name==null){
            if(chunkIndex==0){
                name=UUID.randomUUID().toString();
                //把这个文件标识为这个用户的
                handler.newFileUpload(name,userId);
                //同时使用延迟队列检测到一定时间时文件是否完成传输，如果没有就删除minio中整个文件夹
                RemoveDelayedFile removeFile = new RemoveDelayedFile();
                removeFile.setUserId(userId);
                removeFile.setFilename(name);
                removeFile.setFolder(localPath);
                removeFile.setChunkTotal(chunkTotal);
                rabbitTemplate.convertAndSend(RabbitMQConst.MINIO_FILE_EXCHANGE_NAME,RabbitMQConst.REMOVE_FILE_DELAYED_QUEUE_KEY,removeFile);

            }else{
                return Result.fail("文件没有名字");
            }
        }
        File file1 = new File(localPath + name + chunkIndex + ".temp");
        try {
            if(file1.exists()){
                log.info("{}文件分块{}已存在",name,chunkIndex);
                return Result.success();
            }
            file1.createNewFile();
            file.transferTo(file1);
        }catch (IOException e){
            return Result.fail("创建临时文件失败");
        }

        /*//在minio上的路径./userId/uuid/index
        String minioPrefix=userId+"/"+name+"/";
        try {
            uploadFile(minioPrefix+chunkIndex,file1.getPath(),extension);
        } catch(FileExistsException e){
            log.info("{}文件分块{}已存在",name,chunkIndex);
            return Result.success();
        }catch(Exception e){
            e.printStackTrace();
            return Result.fail("传输文件分块"+chunkIndex+"失败");
        }
        //上次文件完成之后删除临时文件
        file1.delete();*/

        //插入完之后检查是否分块已经插入完毕
        if(chunkIndex==chunkTotal-1){
            log.info("开始合并文件{}",name);
            Time time=null;
            try {
                 time= composeFile(name, chunkTotal, extension);
            } catch (Exception e) {
                e.printStackTrace();
                log.info("合并文件{}失败",name);
                return Result.fail("合并失败");
            }
            //完成合并，修改数据库内表的状态


            //然后把延迟队列中的对应的定时删除任务删除
            RemoveDelayed delayed = new RemoveDelayed();
            delayed.setUserId(userId);
            delayed.setFilename(name);
            rabbitTemplate.convertAndSend(RabbitMQConst.MINIO_FILE_EXCHANGE_NAME,RabbitMQConst.REMOVE_DELAYED_QUEUE_KEY,delayed);

            //获得经验
            //todo 远程调用，还得搞个分布式事务
            infoClient.addExp(new Pair(3+"",ThreadUtils.get()));

            VideoChunkVO vo = new VideoChunkVO();
            vo.setPath(configProperties.getAccess_path()+"/"+bucket+"/"+userId+"/"+name+"."+extension);
            vo.setFilename(name);
            BeanUtils.copyProperties(time,vo);
            //插入存储记录
            handler.updateFileUploadStatus(name,vo.getPath());  //异步更新数据库
            //最后删除redis
            JedisUtil.getJedis().del(usedId+":"+name);
            return Result.success(vo);
        }
        if(chunkIndex==0){
            VideoChunkVO vo = new VideoChunkVO();
            vo.setFilename(name);
            //把name插入redis中，之后作为检测
            JedisUtil.getJedis().setex(usedId+":"+name,minioConfigProperties.getTtl(),"0/"+chunkTotal);//值为目前进度
            return Result.success(vo);
        }
        JedisUtil.getJedis().setex(usedId+":"+name,minioConfigProperties.getTtl(),chunkIndex+"/"+chunkTotal);
        return Result.success();
    }

    @Override
    @Transactional
    public Result cancelUploadChunk(String name) {
        Integer usedId=ThreadUtils.get();
        if(JedisUtil.getJedis().exists(usedId+":"+name)) {
            //说明还没有上传完成
            JedisUtil.getJedis().del(usedId + ":" + name);
            //异步
            handler.delFileUpload(name,ThreadUtils.get());
        }else{
            //上传完成，不能删除
            return Result.fail("已经上传完成，不能删除");
        }
        //文件过了过期时间会自动删除的
        //还得删除数据库中这个文件的归属

        return Result.success();
    }

    public String handleFilenameInRedis(String key){
        int begin = key.indexOf(":");
        return key.substring(begin+1);
    }
    public boolean checkExist(String name,Integer usedId){//检测文件是否存在
        return JedisUtil.getJedis().exists(usedId+":"+name);
    }
    public Result<VideoChunkVO> getChunkTotal(String name,Integer usedId,Integer chunkTotal,Integer chunkIndex){
        String s = JedisUtil.getJedis().get(usedId + ":" + name);
        String[] split = s.split("/");
        int trueTotal = Integer.parseInt(split[1]);
        int trueIndex = Integer.parseInt(split[0]);
        if(chunkTotal!= trueTotal ||chunkIndex!= trueIndex){
            log.info("总块数或index不正确,传输的块数{}!=正确块数{}，索引{}!=正确索引{}",chunkTotal,trueTotal,chunkIndex,trueIndex);
            VideoChunkVO t = new VideoChunkVO();
            t.setType(2);
            t.setTotalProcess(trueTotal);
            t.setProcessIndex(trueIndex);
            return Result.success(t);
        }
        return null;
    }
    @Transactional
    public Result delVideo(String filename,String extension){
        RemoveObjectArgs build = RemoveObjectArgs.builder().bucket(bucket).object(ThreadUtils.get() + "/" + filename + "." + extension).build();
        try {
            client.removeObject(build);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("删除文件失败");
            return Result.fail("删除文件失败");
        }
        return Result.success();
    }
    @Autowired
    private Jedis jedis;
    @Override
    public Result<DanmuVO> getDanmu(Integer requestId, String videoId, Integer timestamp) {//可以选择建立videoId和timestamp的索引
        //还得判断这个时间戳是否是正确的，在缓存中判断，还得考虑缓存一致性问题
        String lenKey = RedisPrefix.VIDEO_LENGTH + videoId;
        String lenLock=RedisPrefix.VIDEO_LENGTH_LOCK+videoId;
        if(!jedis.exists(lenKey)){
            //从数据库中装载
            try {
                while (jedis.setnx(lenLock, "") == 0) ;//没有设置锁超时，所以不用担心删除的锁不是自己的
                if (!jedis.exists(lenKey)) {
                    //装载
                    jedis.set(lenKey,videoItemMapper.selectById(videoId).getMaxTimeStamp().toString());
                }
            }finally {
                jedis.del(lenLock);
            }
        }
        int maxTimeStamp = Integer.parseInt(jedis.get(lenKey));
        if(maxTimeStamp<timestamp){
            return Result.fail("时间戳无效");
        }
        //先在redis中获取
        int backZero = getBackZero(timestamp);
        String intervalLockKey=RedisPrefix.INTERVAL+videoId+"_"+backZero;
        if(jedis.setnx(intervalLockKey,"")==0){//原子操作，这个key是来验证是否存在区间数据，所以在区间数据被删除时，也需要删除这个
            //同步添加区间数据
            loadInterval(backZero,videoId);
        }
        //还得验证是否需要预热下一区间的数据
        if(backZero+10<=maxTimeStamp&&timestamp%10==8){//当以8结尾时
            //预热，添加到消息队列中
            //验证下一个区间是否已经添加
            String nextIntervalLockKey=RedisPrefix.INTERVAL+videoId+"_"+(backZero+10);
            if(jedis.setnx(nextIntervalLockKey,"")==0){
                //异步添加区间数据
                LoadIntervalDTO dto = new LoadIntervalDTO(backZero+10, videoId);
                rabbitTemplate.convertAndSend(RabbitMQConst.DANMU_LOADER_EXCHANGE_NAME,RabbitMQConst.DANMU_LOADER_KEY,dto);
            }
        }
        String goal=RedisPrefix.SINGLE_POINT+videoId+"_"+timestamp;
        while(!jedis.exists(goal));//空转，等待装载数据
        //返回的是json数据，直接返回
        List<String> lrange = jedis.lrange(goal, 0, -1);
        if(lrange.size()!=0) {
            DanmuVO vo = new DanmuVO();
            vo.setRequestId(requestId);
            vo.setTimestamp(timestamp);
            vo.setVideoId(videoId);
            vo.setDanmuList(lrange);
            return Result.success(vo);
        }
        return Result.success();    //区分没有数据和有数据的情况
    }
    @Transactional
    @Override
    public Result sendDanmu(SendDanmuVO vo) {
        String goal=RedisPrefix.SINGLE_POINT+vo.getVideoId()+"_"+vo.getTimestamp();
        if(jedis.exists(goal)){
            DanmuOutline outline = new DanmuOutline();
            outline.setContent(vo.getContent());
            outline.setTime(vo.getTimestamp());
            jedis.rpush(goal,JSON.toJSONString(outline));
        }
        //异步写入数据库
        rabbitTemplate.convertAndSend(RabbitMQConst.DANMU_LOADER_EXCHANGE_NAME,RabbitMQConst.DANMU_STORE_KEY,vo);
        return Result.success();
    }

    public void loadInterval(int backZero,String videoId){
        //防止重复查询
        for (int i = 0; i < 10; i++) {
            String singlePointKey=RedisPrefix.SINGLE_POINT+videoId+"_"+(backZero+i);
            List<Danmu> danmus = danmuMapper.selectList(Wrappers.<Danmu>lambdaQuery().eq(Danmu::getVideoItemId, videoId).eq(Danmu::getTime, backZero + i));
            String[] list = (String[])(danmus.stream().map(danmu -> {
                DanmuOutline outline = new DanmuOutline();
                BeanUtils.copyProperties(danmu, outline);
                return JSON.toJSONString(outline);
            }).toArray());
            jedis.rpush(singlePointKey,list);
        }
    }
    public int getBackZero(Integer timestamp){
        return timestamp-timestamp%10;
    }

    public void encodeMp4(String filename,String newname){
        log.info("{}转码中",filename);
        File source = new File(localPath+filename);
        File target = new File(localPath+newname);
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("libmp3lame");//音频编码格式
        //audio.setBitRate(new Integer(56000));//设置比特率，比特率越大，转出来的音频越大（默认是128000，最好默认就行，有特殊要求再设置）
        audio.setChannels(1);
        audio.setSamplingRate(22050);
        VideoAttributes video = new VideoAttributes();
        video.setCodec("libx264");//视屏编码格式
        //video.setBitRate(new Integer(56000));//设置比特率，比特率越大，转出来的视频越大（默认是128000,最好默认就行，有特殊要求再设置）
        video.setFrameRate(15);//数值设置小了，视屏会卡顿
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("mp4");
        attrs.setAudioAttributes(audio);
        attrs.setVideoAttributes(video);
        Encoder encoder = new Encoder();
        MultimediaObject multimediaObject=new MultimediaObject(source);
        try {
            encoder.encode(multimediaObject,target,attrs);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }catch (InputFormatException e){
            e.printStackTrace();
        }catch (EncoderException e){
            e.printStackTrace();
        }
        log.info("{}转码完成",newname);
    }
    @Transactional
    @Override
    public Result<String> uploadImg(MultipartFile file) {
        String filename = file.getOriginalFilename();
        String extension=filename.substring(filename.lastIndexOf('.')+1);
        InputStream inputStream =null;
        String minioPath=UUID.randomUUID().toString()+"."+extension;
        try {
            inputStream   = file.getInputStream();
        } catch (IOException e) {
            return Result.fail("文件为空");
        }
        try {
            uploadImg(minioPath, inputStream,extension);
        } catch (Exception e) {
            Result.fail("文件上传失败");
        }
        String path=configProperties.getAccess_path()+"/"+imgBucket+"/"+minioPath;
        fileStorageMapper.insert(new FileStorage(path, FileStorageType.IMG_TYPE));
        return Result.success(path);
    }
    @Transactional
    @Override
    public Result submit(SubmitDTO dto) {
        int i = checkVideo(dto.getVideos());
        if(i!=-1){
            log.info("第"+i+1+"个视频不存在");
            return Result.fail("第"+i+1+"个视频不存在");
        }
        if (!fileExist(dto.getCoverImageLink(), FileStorageType.IMG_TYPE)) {
            log.info("封面不存在");
            return Result.fail("封面不存在");
        }

        Video video = new Video();
        video.setUserId(ThreadUtils.get());

        BeanUtils.copyProperties(dto,video);
        videoMapper.insert(video);

        String videoId = video.getVideoId();
        if(videoId ==null){
            System.out.println("插入时似乎不会给对象赋予id");
        }

        if(dto.getReleaseTime()!=null){
            //用消息队列实现定时发布
            sendReleaseMessage(dto.getReleaseTime(),videoId);
        }else{
            video.setReleaseTime(LocalDateTime.now());
            video.setIsPublished(true);
        }

         Integer totalHours=0;
         Integer totalMinutes=0;
         Integer totalSeconds=0;

        List<VideoInfo> videos = dto.getVideos();
        for (VideoInfo info : videos) {
            VideoItem item = new VideoItem();
            item.setVideoId(videoId);
            BeanUtils.copyProperties(info,item);
            videoItemMapper.insert(item);

            totalHours+=item.getHours();
            totalMinutes+=item.getMinutes();
            totalSeconds+=item.getSeconds();
        }
        video.setTotalHours(totalHours);
        video.setTotalMinutes(totalMinutes);
        video.setTotalSeconds(totalSeconds);
        videoMapper.updateById(video);

        List<String> tags = dto.getTags();
        for (String tag : tags) {
            Tag tag1 = new Tag();
            tag1.setTag(tag);
            tag1.setVideoId(videoId);
            tagMapper.insert(tag1);
        }

        //添加投稿数量：
        userInfoMapper.updatePubs(ThreadUtils.get());
        return Result.success();
    }

    @Override
    public Result<VideoContent> getVideo(String videoId) {//相当于观看视频
        Video video = videoMapper.selectById(videoId);
        if(video==null){
            return Result.fail("视频不存在");
        }
        if(!video.getIsPublished()){
            return Result.fail("视频未发布");
        }
        //增加播放量
        video.setPlays(video.getPlays()+1);
        videoMapper.updateById(video);
        if(everydayPlaysMapper.exists(Wrappers.<EverydayPlays>lambdaQuery().eq(EverydayPlays::getUserId,video.getUserId()).eq(EverydayPlays::getDate,LocalDate.now()))) {
            everydayPlaysMapper.addEverydayPlays(video.getUserId(), LocalDate.now());
        }else{
            EverydayPlays plays = new EverydayPlays();
            plays.setDate(LocalDate.now());
            plays.setUserId(video.getUserId());
            plays.setEverydayPlays(0);
            everydayPlaysMapper.insert(plays);
        }

        VideoContent content = new VideoContent();
        BeanUtils.copyProperties(video,content);

        List<VideoInfo> list = videoItemMapper.selectList(Wrappers.<VideoItem>lambdaQuery().eq(VideoItem::getVideoId, videoId)).stream().map(item->{
            VideoInfo info = new VideoInfo();
            BeanUtils.copyProperties(item,info);
            return info;
        }).toList();
        content.setVideoInfos(list);

        List<String> tags = tagMapper.selectList(Wrappers.<Tag>lambdaQuery().eq(Tag::getVideoId, videoId)).stream().map(Tag::getTag).toList();
        content.setTags(tags);


        log.info("调用userinfo的getUserInfo接口");
        UserInfo data = infoClient.getUserInfo(video.getUserId()).getData();
        content.setUserId(data.getUserId());
        content.setUserName(data.getUserName());
        content.setUserImage(data.getUserImage());
        content.setSignature(data.getSignature());
        content.setFans(data.getFans());

        //可以搞异步
        data.setPlays(data.getPlays()+1);
        infoClient.updateInfo(data);


        //更新每日任务和经验
        DailyMission mission = infoClient.getDailyMission(ThreadUtils.get()).getData();
        mission.setDailyWatch(true);
        infoClient.editDailyMission(mission);

        //经验
        infoClient.addExp(new Pair(3+"",ThreadUtils.get()));

        //TODO 是否为粉丝
        content.setIsFollowed(infoClient.isFans(new FansPair(video.getUserId(),ThreadUtils.get())).getData());

        content.setIsLike(likeRecordMapper.exists(Wrappers.<LikeRecord>lambdaQuery().eq(LikeRecord::getUserId,ThreadUtils.get()).eq(LikeRecord::getVideoId,videoId)));
        content.setIsCoins(coinsRecordMapper.exists(Wrappers.<CoinsRecord>lambdaQuery().eq(CoinsRecord::getUserId,ThreadUtils.get()).eq(CoinsRecord::getVideoId,videoId).gt(CoinsRecord::getCoins,1)));
        return Result.success(content);
    }

    @Override
    public PageResult<List<VideoDongtai>> getVideoDongtai(Integer pageSize, Integer pageNum) {
        Integer userId=ThreadUtils.get();
        log.info("userId为{}",userId);
        List<Integer> data = infoClient.getFollowing(userId).getData();
        Page<Video> page = new Page<>(pageNum, pageSize);
        Page<Video> page1 = videoMapper.selectPage(page, Wrappers.<Video>lambdaQuery().select(Video::getVideoId, Video::getCoverImageLink, Video::getTitle, Video::getUserId)
                .in(Video::getUserId, data).eq(Video::getIsPublished, true).orderByDesc(Video::getReleaseTime));
        if(pageNum>page1.getPages()){
            return PageResult.end();
        }
        List<VideoDongtai> list = page1.getRecords().stream().map(video -> {
            Integer id = video.getUserId();
            UserInfo info = infoClient.getUserInfo(id).getData();
            VideoDongtai dongtai = new VideoDongtai();
            BeanUtils.copyProperties(info, dongtai);
            BeanUtils.copyProperties(video, dongtai);
            return dongtai;
        }).toList();
        if(pageNum==page1.getPages()){
            return PageResult.success(list,page1.getTotal(),pageNum,pageSize,true);
        }
        return PageResult.success(list,page1.getTotal(),pageNum,pageSize);
    }
    public VideoOutline getVideoOutline(String videoId){//获取视频的概览
        Video video = videoMapper.selectById(videoId);
        if(video==null){
            log.info("{}的视频不存在",videoId);
            return null;
        }
        VideoOutline outline = new VideoOutline();
        BeanUtils.copyProperties(video,outline);
        //TODO 还有一个name
        log.info("调用userinfo的getUserInfo接口");
        outline.setName(infoClient.getUserInfo(video.getUserId()).getData().getUserName());
        return outline;
    }
    @Transactional
    public void sendReleaseMessage(LocalDateTime time,String videoId){
        if(time.isBefore(LocalDateTime.now())){
            return;
        }
        Duration between = Duration.between(LocalDateTime.now(), time);
        long millis = between.toMillis();
        MessageProperties properties = new MessageProperties();
        properties.setExpiration(millis+"");
        Message message = new Message((videoId + "").getBytes(), properties);
        rabbitTemplate.convertAndSend(RabbitMQConst.DEAD_EXCHANGE_NAME,RabbitMQConst.DEAD_RELEASE_QUEUE_KEY,message);
    }
    public int checkVideo(List<VideoInfo> infos){
        for (int i = 0; i < infos.size(); i++) {
            VideoInfo info = infos.get(i);
            if(!fileExist(info.getVideoLink(),FileStorageType.VIDEO_TYPE)){
                return i;
            }
        }
        return -1;
    }
    public Boolean fileExist(String filename,String filetype){
        FileStorage one = fileStorageMapper.selectOne(Wrappers.<FileStorage>lambdaQuery().eq(FileStorage::getFilename, filename).eq(FileStorage::getFileType, filetype));
        return one!=null;
    }
    @Transactional
    public void uploadImg(String minioPath,InputStream inputStream,String extension) throws IOException, ServerException, InvalidBucketNameException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket(imgBucket)
                .object(minioPath)
                .stream(inputStream, inputStream.available(), -1)
                .contentType(transferToMIME(extension))
                .build();
        client.putObject(args);
    }
    @Transactional
    public void uploadFile(String minioPath,String filePath,String extension) throws IOException, ServerException, InvalidBucketNameException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        //先检测是否存在切片
        log.info("传输分块文件到路径{}",minioPath);
        GetObjectArgs args = GetObjectArgs.builder()
                .bucket(bucket)
                .object(minioPath)
                .build();
        InputStream stream=null;
        try {
            stream = client.getObject(args);
        }catch (Exception e){

        }
        if(stream!=null){
            throw new FileExistsException();
        }

        String mime=transferToMIME(extension);

        UploadObjectArgs build = UploadObjectArgs.builder()
                .bucket(bucket)
                .filename(filePath)
                .object(minioPath)
                .contentType(mime)
                .build();
        client.uploadObject(build);
    }
    public String transferToMIME(String extension){
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
        if(extensionMatch!=null){
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }
    public Time composeFile(String filename,int chunkTotal,String extension) throws IOException, ServerException, InvalidBucketNameException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        File merge = new File(localPath + filename + "." + extension);
        try(RandomAccessFile randomAccessFile = new RandomAccessFile(merge, "rw");) {
            byte[] bytes = new byte[1024];
            int len;
            for (int i = 0; i < chunkTotal; i++) {
                File file = new File(localPath + filename + i + ".temp");
                try (RandomAccessFile accessFile = new RandomAccessFile(file, "r");) {
                    while ((len = accessFile.read(bytes)) != -1) {
                        randomAccessFile.write(bytes, 0, len);
                    }
                }
                //todo 搞个删除本地文件的mq
            }
        }
        String newfilename=filename+chunkTotal+"."+extension;
        encodeMp4(filename+"."+extension,newfilename);
        uploadFile(ThreadUtils.get()+"/"+filename+"."+extension,localPath+newfilename,extension);
        //发送完成之后删除文件
        RemoveFile file = new RemoveFile(localPath,filename,chunkTotal,extension);
        rabbitTemplate.convertAndSend(RabbitMQConst.MINIO_FILE_EXCHANGE_NAME,RabbitMQConst.REMOVE_FILE_QUEUE_KEY,file);
        //获得时间
        return getTime(VideoUtil.getDuration(merge.getPath()));
    }
    public Time getTime(long seconds){
        Time time = new Time();
        time.setHours((int)seconds/3600);
        seconds-=3600*time.getHours();
        time.setMinutes((int)seconds/60);
        seconds-=60*time.getHours();
        time.setSeconds((int)seconds);
        return time;
    }
    public void composeFile(Integer userId,String name,String extension,String bucket,Integer chunkTotal){//合并文件
        List<ComposeSource> list=new ArrayList<>();
        String prefix=userId+"/"+name+"/";
        for (int i = 0; i < chunkTotal; i++) {
            ComposeSource source = ComposeSource.builder()
                    .bucket(bucket)
                    .object(prefix + i)
                    .build();
            list.add(source);
        }
        ComposeObjectArgs args = ComposeObjectArgs.builder()
                .bucket(bucket)
                .object(userId+"/"+name + "." + extension)
                .sources(list)
                .build();
        try {
            client.composeObject(args);
        }catch(Exception e){
            e.printStackTrace();
            log.error("合并{}文件失败",prefix);
        }
        //删除其他文件,可以来个异步任务删除
        /*for (int i = 0; i < chunkTotal; i++) {
            RemoveFile file = new RemoveFile(prefix + i,bucket);
            rabbitTemplate.convertAndSend(RabbitMQConst.MINIO_FILE_EXCHANGE_NAME,RabbitMQConst.REMOVE_FILE_QUEUE_KEY,file);
        }*/
    }
    @Transactional
    public Result like(String videoId){
        Jedis jedis = JedisUtil.getJedis();
        //用redis保证幂等性，如果redis中没有就去数据库里查找

        Integer userId=ThreadUtils.get();
        String likeKey="vlike_"+userId+"_"+videoId;
        String vlikeKey="vlike_"+videoId;

        if(jedis.exists(likeKey)){
            return Result.fail("已经喜欢了");
        }else{
            //还得从数据库里查
            //还得加锁
            RLock lock = redissonClient.getLock(likeKey + "_lock");
            try{
                lock.lock();
                if(jedis.exists(likeKey)) {
                    if (likeRecordMapper.exists(Wrappers.<LikeRecord>lambdaQuery().eq(LikeRecord::getVideoId, videoId).eq(LikeRecord::getUserId, userId))) {
                        jedis.setex(likeKey, RedisExpireTime.LIKE,"");
                        return Result.fail("已经喜欢了");
                    }
                }
            }finally {
                lock.unlock();
            }
            if(jedis.exists(vlikeKey)){
                jedis.incr(vlikeKey);
                jedis.setex(likeKey, RedisExpireTime.LIKE,"");
            }else{
                //得使用分布式锁获取
                RLock lock1 = redissonClient.getLock(vlikeKey + "_lock");
                try{
                    lock.lock();
                    if(jedis.exists(vlikeKey)){
                        Video video = videoMapper.selectOne(Wrappers.<Video>lambdaQuery().select(Video::getLikes).eq(Video::getVideoId, videoId));
                        jedis.setex(vlikeKey,RedisExpireTime.VIDEO_LIKE,""+video.getLikes());
                    }
                }finally {
                    lock1.unlock();
                }
                jedis.incr(vlikeKey);
                jedis.setex(likeKey, RedisExpireTime.LIKE,"");
            }
            //异步增加
            handler.updateLike(videoId,userId);
        }

        return Result.success();
    }
    @Transactional
    public Result giveCoins(String videoId,Integer count){
        Video video = videoMapper.selectById(videoId);
        if(video==null){
            return Result.fail("视频无效");
        }
        if(count>2||count<=0){
            return Result.fail("投币数量"+count+"错误");
        }
        //还需要检查硬币是否足够
        UserInfo userInfo = infoClient.getUserInfo(ThreadUtils.get()).getData();
        if(userInfo.getCoins()<count){
            return Result.fail("硬币不够");
        }else{
            userInfo.setCoins(userInfo.getCoins()-count);
            infoClient.updateInfo(userInfo);

            //插入硬币消费记录
            CoinsAddRecord record = new CoinsAddRecord();
            record.setUserId(ThreadUtils.get());
            record.setCount(-1*count);
            record.setTime(LocalDateTime.now());
            record.setReason("投币");
            addRecordMapper.insert(record);
        }
        //todo 添加投币记录
        CoinsRecord coinsRecord = new CoinsRecord();
        coinsRecord.setUserId(ThreadUtils.get());
        coinsRecord.setCoins(count);
        coinsRecord.setVideoId(videoId);
        coinsRecord.setTime(LocalDateTime.now());
        coinsRecordMapper.insert(coinsRecord);

        video.setCoins(video.getCoins()+count);
        videoMapper.updateById(video);
        return Result.success();
    }
    @Transactional
    public Result addMark(String videoId){
        Video video = videoMapper.selectById(videoId);
        if(video==null){
            return Result.fail("视频无效");
        }
        video.setMarks(video.getMarks()+1);
        videoMapper.updateById(video);

        //添加用户评论
        return Result.success();
    }

    @Override
    public PageResult<List<VideoOutline>> getLikeVideos(Integer userId,Integer pageNum,Integer pageSize) {
        Page<LikeRecord> page = new Page<>(pageNum, pageSize);
        Page<LikeRecord> page1 = likeRecordMapper.selectPage(page, Wrappers.<LikeRecord>lambdaQuery().eq(LikeRecord::getUserId, userId).orderByDesc(LikeRecord::getTime));
        if(pageNum>page1.getPages()){
            return PageResult.end();
        }
        List<VideoOutline> list = page1.getRecords().stream().map(record -> {
            return getVideoOutline(record.getVideoId());
        }).toList();
        if(pageNum==page1.getPages()){
            return PageResult.success(list,page1.getTotal(),pageNum,pageSize,true);
        }
        return PageResult.success(list,page1.getTotal(),pageNum,pageSize);
    }
    public PageResult<List<VideoOutline>> getCoinsVideos(Integer userId,Integer pageNum,Integer pageSize){
        Page<CoinsRecord> page = new Page<>(pageNum, pageSize);
        Page<CoinsRecord> page1 = coinsRecordMapper.selectPage(page, Wrappers.<CoinsRecord>lambdaQuery().eq(CoinsRecord::getUserId, userId).orderByDesc(CoinsRecord::getTime));
        if(pageNum>page1.getPages()){
            return PageResult.end();
        }
        List<VideoOutline> list = page1.getRecords().stream().map(record -> {
            return getVideoOutline(record.getVideoId());
        }).toList();
        if(pageNum==page1.getPages()){
            return PageResult.success(list,page1.getTotal(),pageNum,pageSize,true);
        }
        return PageResult.success(list,page1.getTotal(),pageNum,pageSize);
    }

    @Override
    public PageResult<List<VideoOutline>> getMyVideo(Integer userId, Integer pageNum, Integer pageSize) {
        Page<Video> page = new Page<>(pageNum, pageSize);
        Page<Video> page1 = videoMapper.selectPage(page, Wrappers.<Video>lambdaQuery().eq(Video::getUserId, userId).orderByDesc(Video::getReleaseTime));
        if(pageNum>page1.getPages()){
            return PageResult.end();
        }
        List<VideoOutline> list = page1.getRecords().stream().map(video -> {
            VideoOutline outline = new VideoOutline();
            BeanUtils.copyProperties(video, outline);
            //TODO 还有一个name
            log.info("调用userinfo的getUserInfo接口");
            outline.setName(infoClient.getUserInfo(video.getUserId()).getData().getUserName());
            return outline;
        }).toList();
        if(pageNum==page1.getPages()){
            return PageResult.success(list,page1.getTotal(),pageNum,pageSize,true);
        }
        return PageResult.success(list,page1.getTotal(),pageNum,pageSize);
    }

    @Override
    public PageResult<List<VideoOutline>> getPartitionVideo(@RequestBody PartitionVideoDTO dto) {
        Integer pageNum=dto.getPageNum();
        Integer pageSize=dto.getPageSize();
        Page<Video> page = new Page<>(pageNum,pageSize);
        Page<Video> page1 = videoMapper.selectPage(page, Wrappers.<Video>lambdaQuery().eq(dto.getPartitions() != null, Video::getPartitions, dto.getPartitions())
                .eq(dto.getSubPartition() != null, Video::getSubPartition, dto.getSubPartition()).orderByDesc(Video::getWeight));

        if(pageNum>page1.getPages()){
            return PageResult.end();
        }

        List<VideoOutline> list = page1.getRecords().stream().map(video -> {
            VideoOutline outline = new VideoOutline();
            BeanUtils.copyProperties(video, outline);
            //TODO 还有一个name
            log.info("调用userinfo的getUserInfo接口");
            outline.setName(infoClient.getUserInfo(video.getUserId()).getData().getUserName());
            return outline;
        }).toList();
        if(pageNum==page1.getPages()){
            return PageResult.success(list,page1.getTotal(),pageNum,pageSize,true);
        }
        return PageResult.success(list,page1.getTotal(),pageNum,pageSize);
    }


}
