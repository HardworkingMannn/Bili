package org.example.video.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.Model.constant.ParentType;
import org.example.Model.constant.PartitionConst;
import org.example.Model.entity.*;
import org.example.Model.pojo.*;
import org.example.video.Model.pojo.Result;
import org.example.video.mapper.*;
import org.example.video.pojo.SubmitDTO;
import org.example.video.pojo.VideoChunkVO;
import org.example.video.pojo.VideoContent;
import org.example.video.pojo.VideoDongtai;
import org.example.video.service.UploadVideoService;
import org.example.video.utils.JedisUtil;
import org.example.video.utils.MarkClient;
import org.example.video.utils.ThreadUtils;
import org.example.video.utils.VideoHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@Slf4j
@Tag(name="上传视频文件")
@RequestMapping("/video")
public class UploadVideoController {
    @Autowired
    private UploadVideoService uploadVideoService;
    @Autowired
    private EverydayPlaysMapper everydayPlaysMapper;
    @Autowired
    private EverydayLikesMapper everydayLikesMapper;
    @Autowired
    private EverydayCoinsMapper everydayCoinsMapper;
    @Autowired
    private EverydayCollectsMapper everydayCollectsMapper;
    @Autowired
    private VideoMapper videoMapper;
    @Autowired
    private UserInfoMapper infoMapper;
    @Autowired
    private MarkClient markClient;
    @Autowired
    private LikeRecordMapper likeRecordMapper;
    @Autowired
    private CoinsRecordMapper coinsRecordMapper;
    @Autowired
    private LikeNotifyMapper likeNotifyMapper;
    @PostMapping("/upload")
    @Operation(summary = "上传文件分块",
        parameters = {
            @Parameter(name="file",description = "分块文件"),
                @Parameter(name="chunkIndex",description = "分块序号，从0开始"),
                @Parameter(name="chunkTotal",description = "分块大小，注意每次分块大小需一致，而且最小为5MB，除了最后一块"),
                @Parameter(name="name",description = "文件名字，第一次传输不需要输入，第一个传输之后接口返回文件名字，之后每一次传输都需要"),
                @Parameter(name="extension",description = "文件拓展名，每一次都需要输入")
        })
    public Result<VideoChunkVO> uploadChunk(@RequestPart("file")MultipartFile file, Integer chunkIndex, Integer chunkTotal, @RequestParam(required = false) String name, String extension){
        return uploadVideoService.uploadChunk(file, chunkIndex, chunkTotal, name,extension);
    }
    @PostMapping("/img")
    @Operation(summary = "上传图片",parameters = {
            @Parameter(name="file",description = "上传的图片文件")
    },
    responses = {
            @ApiResponse(description = "返回图片链接")
    })
    public Result<String> uploadImg(@RequestPart("file") MultipartFile file){
        return uploadVideoService.uploadImg(file);
    }
    @PostMapping("/submit")
    @Operation(summary = "视频投稿接口")
    public Result submit(@RequestBody SubmitDTO dto){
        return uploadVideoService.submit(dto);
    }
    @PostMapping("/getVideo/{videoId}")
    @Operation(summary = "进入视频页面")
    public Result<VideoContent> getVideo(@PathVariable String videoId){
        return uploadVideoService.getVideo(videoId);
    }

    @GetMapping("/getVideoDongtai")
    @Operation(summary = "获取视频动态",
    parameters = {@Parameter(name="pageSize",description = "页长，表示一次获取多少个"),
    @Parameter(name="pageNum",description = "页数，表示这是第几次获取，从0开始")})
    public PageResult<List<VideoDongtai>> getVideoDongtai(Integer pageNum, Integer pageSize){
        return uploadVideoService.getVideoDongtai(pageSize, pageNum);
    }
    @PostMapping("/getVideoOutlines")
    @Operation(summary = "获取大量视频概览，内部接口")
    public Result<List<VideoOutline>> getVideoOutlines(@RequestBody List<String> ids){
        log.info("进入getVideoOutlines接口，参数为：{}",ids);
        List<VideoOutline> list = ids.stream().map(id -> {
            return uploadVideoService.getVideoOutline(id);
        }).toList();
        return Result.success(list);
    }
    @GetMapping("/getVideoOutline")
    @Operation(summary = "获取视频概览，内部接口")
    public Result<VideoOutline> getVideoOutline(String videoId){
        VideoOutline outline = uploadVideoService.getVideoOutline(videoId);
        return Result.success(outline);
    }
    @GetMapping("/like")
    @Operation(summary = "喜欢视频")
    public Result like(String videoId){
        return uploadVideoService.like(videoId);
    }
    @GetMapping("/giveCoins")
    @Operation(summary = "投币")
    public Result giveCoins(String videoId,Integer count){
        return uploadVideoService.giveCoins(videoId, count);
    }
    @GetMapping("/addMark")
    @Operation(summary = "添加评论，内部接口")
    public Result addMark(String videoId){
        return uploadVideoService.addMark(videoId);
    }
    @GetMapping("/getLikeVideos")
    @Operation(summary = "获取喜欢视频的记录")
    public PageResult<List<VideoOutline>> getLikeVideos(Integer userId,Integer pageNum,Integer pageSize){
        if(userId==null){
            return PageResult.fail("id为空");
        }
        return uploadVideoService.getLikeVideos(userId, pageNum, pageSize);
    }
    @GetMapping("/getCoinsVideos")
    @Operation(summary = "获取投币视频的记录")
    public PageResult<List<VideoOutline>> getCoinsVideos(Integer userId,Integer pageNum,Integer pageSize){
        if(userId==null){
            return PageResult.fail("id为空");
        }
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(10);
        ReentrantLock lock = new ReentrantLock();
        Condition condition=lock.newCondition();
        Timer timer=new Timer();
        CyclicBarrier cyclicBarrier=new CyclicBarrier(11);
        
        return uploadVideoService.getCoinsVideos(userId,pageNum,pageSize);
    }
    @GetMapping("/getMyVideo")
    @Operation(summary = "获取我的投稿，获取其他人的也可以调用这个接口")
    public PageResult<List<VideoOutline>> getMyVideo(Integer userId,Integer pageNum,Integer pageSize){
        if(userId==null){
            return PageResult.fail("id为空");
        }
        return uploadVideoService.getMyVideo(userId, pageNum, pageSize);
    }
    @Autowired
    private VideoHandler videoHandler;
    @GetMapping("/getHomePageVideo")
    @Operation(summary = "获取主页视频")
    public PageResult<List<VideoOutline>> getHomePageVideo(Integer pageNum,Integer pageSize){
        log.info("访问主页接口");
        List<VideoOutline> list=null;
        Jedis jedis=null;
        if(pageNum>100/pageSize+1){
            return PageResult.end();
        }
        try {
            jedis = JedisUtil.getJedis();
            if(!jedis.exists(VideoHandler.WEIGHT_LIST)){
                log.info("redis的热度列表为空，需要获取");
                videoHandler.WeightUpdater();
            }
            list = jedis.zrange(VideoHandler.WEIGHT_LIST, (pageNum-1) * pageSize, pageNum * pageSize-1).stream().map(json -> {
                return JSON.parseObject(json, VideoOutline.class);
            }).toList();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }
        if(pageNum==100/pageSize+1){
            return PageResult.success(list,100l,pageNum,pageSize,true);
        }
        return PageResult.success(list,100l,pageNum,pageSize);
    }
    @PostMapping("/getPartitionVideo")
    @Operation(summary = "获取分区视频")
    public PageResult<List<VideoOutline>> getPartitionVideo(@RequestBody PartitionVideoDTO dto){

        return uploadVideoService.getPartitionVideo(dto);
    }
    @GetMapping("/getPartition")
    @Operation(summary = "获取分区")
    public Result<List<PartitionVO>> getPartition(){
        Set<String> set = PartitionConst.map.keySet();
        List<PartitionVO> list1=new ArrayList<>();
        for (String s : set) {
            List<PartitionDTO> list = PartitionConst.map.get(s);
            for (PartitionDTO dto : list) {
                PartitionVO vo = new PartitionVO();
                BeanUtils.copyProperties(dto,vo);
                vo.setPartition(s);
                list1.add(vo);
            }
        }
        return Result.success(list1);
    }
    @GetMapping("/getEverydayPlay")
    @Operation(summary = "获取播放量")
    public PageResult<List<EverydayPlays>> getEverydayPlay(Integer pageNum,Integer pageSize){
        Page<EverydayPlays> page = new Page<>(pageNum, pageSize);
        Page<EverydayPlays> page1 = everydayPlaysMapper.selectPage(page, Wrappers.<EverydayPlays>lambdaQuery().eq(EverydayPlays::getUserId, ThreadUtils.get()).orderByDesc(EverydayPlays::getDate));
        if(pageNum>page1.getPages()){
            return PageResult.end();
        }
        if(pageNum==page1.getPages()){
            return PageResult.success(page1.getRecords(),page1.getTotal(),pageNum,pageSize,true);
        }
        return PageResult.success(page1.getRecords(),page1.getTotal(),pageNum,pageSize);
    }
    @GetMapping("/delVideo")
    @Operation(summary = "删除视频")
    public Result delVideo(String filename,String extension){
        return uploadVideoService.delVideo(filename, extension);
    }
    @GetMapping("/getEverydayLike")
    @Operation(summary = "获取点赞")
    public PageResult<List<EverydayLikes>> getEverydayLike(Integer pageNum, Integer pageSize){
        Page<EverydayLikes> page = new Page<>(pageNum, pageSize);
        Page<EverydayLikes> page1 = everydayLikesMapper.selectPage(page, Wrappers.<EverydayLikes>lambdaQuery().eq(EverydayLikes::getUserId, ThreadUtils.get()).orderByDesc(EverydayLikes::getDate));
        if(pageNum>page1.getPages()){
            return PageResult.end();
        }
        if(pageNum==page1.getPages()){
            return PageResult.success(page1.getRecords(),page1.getTotal(),pageNum,pageSize,true);
        }
        return PageResult.success(page1.getRecords(),page1.getTotal(),pageNum,pageSize);
    }
    @GetMapping("/getEverydayCoin")
    @Operation(summary = "获取硬币")
    public PageResult<List<EverydayCoins>> getEverydayCoin(Integer pageNum, Integer pageSize){
        Page<EverydayCoins> page = new Page<>(pageNum, pageSize);
        Page<EverydayCoins> page1 = everydayCoinsMapper.selectPage(page, Wrappers.<EverydayCoins>lambdaQuery().eq(EverydayCoins::getUserId, ThreadUtils.get()).orderByDesc(EverydayCoins::getDate));
        if(pageNum>page1.getPages()){
            return PageResult.end();
        }
        if(pageNum==page1.getPages()){
            return PageResult.success(page1.getRecords(),page1.getTotal(),pageNum,pageSize,true);
        }
        return PageResult.success(page1.getRecords(),page1.getTotal(),pageNum,pageSize);
    }
    @GetMapping("/getEverydayCollect")
    @Operation(summary = "获取收藏")
    public PageResult<List<EverydayCollects>> getEverydayCollect(Integer pageNum, Integer pageSize){
        Page<EverydayCollects> page = new Page<>(pageNum, pageSize);
        Page<EverydayCollects> page1 = everydayCollectsMapper.selectPage(page, Wrappers.<EverydayCollects>lambdaQuery().eq(EverydayCollects::getUserId, ThreadUtils.get()).orderByDesc(EverydayCollects::getDate));
        if(pageNum>page1.getPages()){
            return PageResult.end();
        }
        if(pageNum==page1.getPages()){
            return PageResult.success(page1.getRecords(),page1.getTotal(),pageNum,pageSize,true);
        }
        return PageResult.success(page1.getRecords(),page1.getTotal(),pageNum,pageSize);
    }
    @GetMapping("/videoManagement")
    @Operation(summary = "视频管理")
    public PageResult<List<VideoManagement>> videoManagement(Integer pageNum, Integer pageSize){
        Page<Video> page = new Page<>(pageNum, pageSize);
        Page<Video> page1 = videoMapper.selectPage(page, Wrappers.<Video>lambdaQuery().eq(Video::getUserId, ThreadUtils.get()).orderByDesc(Video::getReleaseTime));
        if(pageNum>page1.getPages()){
            return PageResult.end();
        }
        List<VideoManagement> list = page1.getRecords().stream().map(video -> {
            VideoManagement management = new VideoManagement();
            BeanUtils.copyProperties(video, management);
            return management;
        }).toList();
        if(pageNum==page1.getPages()){
            return PageResult.success(list,page1.getTotal(),pageNum,pageSize,true);
        }
        return PageResult.success(list,page1.getTotal(),pageNum,pageSize);
    }
    @GetMapping("/delVideos")
    @Operation(summary = "视频管理的删除视频")
    public Result delVideo(String videoId){
        videoMapper.deleteById(videoId);
        return Result.success();
    }
    @GetMapping("/getWeekData")
    @Operation(summary = "获取一周的数据")
    public Result<WeekData> getWeekData(){
        WeekData weekData = new WeekData();
        UserInfo info = infoMapper.selectById(ThreadUtils.get());
        BeanUtils.copyProperties(info,weekData);
        return Result.success(weekData);
    }
    @PostMapping("/forward")
    @Operation(summary = "转发视频")
    public Result forward(String content,String videoId){
        log.info("调用转发视频的接口");
        DongtaiDTO dto = new DongtaiDTO();
        dto.setContent(content);
        dto.setQuoteId(videoId);
        dto.setQuoteType(ParentType.video);
        markClient.publishDongtai(dto);
        return Result.success();
    }
    @GetMapping("/unlike")
    @Operation(summary = "取消点赞")
    public Result unlike(String videoId){
        Video video = videoMapper.selectById(videoId);
        if(video!=null&&likeRecordMapper.exists(Wrappers.<LikeRecord>lambdaQuery().eq(LikeRecord::getUserId,ThreadUtils.get()).eq(LikeRecord::getVideoId,videoId))){
            video.setLikes(video.getLikes()-1);
            videoMapper.updateById(video);
        }else{
            return Result.fail("id不存在");
        }
        likeRecordMapper.delete(Wrappers.<LikeRecord>lambdaQuery().eq(LikeRecord::getUserId,ThreadUtils.get()).eq(LikeRecord::getVideoId,videoId));
        likeNotifyMapper.delete(Wrappers.<LikeNotify>lambdaQuery().eq(LikeNotify::getUserId,ThreadUtils.get()).eq(LikeNotify::getParentId,videoId));
        return Result.success();
    }
    @GetMapping("/uncoins")
    @Operation(summary = "取消投币")
    public Result uncoins(String videoId){
        Video video = videoMapper.selectById(videoId);
        CoinsRecord coinsRecord = coinsRecordMapper.selectOne(Wrappers.<CoinsRecord>lambdaQuery().eq(CoinsRecord::getUserId, ThreadUtils.get()).eq(CoinsRecord::getVideoId, videoId).gt(CoinsRecord::getCoins, 1));
        if(video!=null&& coinsRecord!=null){
            video.setCoins(video.getCoins()-coinsRecord.getCoins());
            videoMapper.updateById(video);
        }else{
            return Result.fail("id不存在");
        }
        coinsRecordMapper.deleteById(coinsRecord);
        return Result.success();
    }
}
