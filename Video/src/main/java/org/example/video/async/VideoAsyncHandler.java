package org.example.video.async;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.Model.entity.LikeRecord;
import org.example.Model.entity.UserInfo;
import org.example.Model.entity.Video;
import org.example.video.Model.pojo.Result;
import org.example.video.constant.FileStorageType;
import org.example.video.entity.FileBelong;
import org.example.video.entity.FileStorage;
import org.example.video.mapper.FileBelongMapper;
import org.example.video.mapper.FileStorageMapper;
import org.example.video.mapper.LikeRecordMapper;
import org.example.video.mapper.VideoMapper;
import org.example.video.utils.ThreadUtils;
import org.example.video.utils.UserInfoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class VideoAsyncHandler {
    @Autowired
    private FileBelongMapper fileBelongMapper;
    @Autowired
    private FileStorageMapper fileStorageMapper;
    @Autowired
    private VideoMapper videoMapper;
    @Autowired
    private LikeRecordMapper likeRecordMapper;
    @Autowired
    private UserInfoClient infoClient;
    @Async
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateFileUploadStatus(String name,String path){
        Integer userId = ThreadUtils.get();
        FileBelong one;
        one= fileBelongMapper.selectList(Wrappers.<FileBelong>lambdaQuery().eq(FileBelong::getUserId, userId).eq(FileBelong::getFilename, name)).get(0);
        one.setStatus(true);
        fileBelongMapper.updateById(one);
        fileStorageMapper.insert(new FileStorage(path, FileStorageType.VIDEO_TYPE));
    }
    @Async
    @Transactional(propagation = Propagation.REQUIRED)
    public void newFileUpload(String name,Integer userId){
        FileBelong entity = new FileBelong();
        entity.setUserId(userId);
        entity.setFilename(name);
        entity.setStatus(false);
        fileBelongMapper.insert(entity);
    }
    @Async
    @Transactional(propagation = Propagation.REQUIRED)
    public void delFileUpload(String name,Integer userId){
        fileBelongMapper.delete(Wrappers.<FileBelong>lambdaQuery().eq(FileBelong::getUserId,userId).eq(FileBelong::getFilename,name));
    }
    @Async
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateLike(String videoId,Integer userId){
        Video video = videoMapper.selectById(videoId);
        if(video==null){
            return;
        }
        //todo 添加喜欢的记录
        LikeRecord one = likeRecordMapper.selectOne(Wrappers.<LikeRecord>lambdaQuery().eq(LikeRecord::getUserId, ThreadUtils.get()).eq(LikeRecord::getVideoId, videoId));
        if(one!=null){
            return;
        }
        LikeRecord likeRecord = new LikeRecord();
        likeRecord.setVideoId(videoId);
        likeRecord.setUserId(ThreadUtils.get());
        likeRecord.setTime(LocalDateTime.now());
        likeRecordMapper.insert(likeRecord);

        //更新通知
       /* LikeNotify likeNotify = notifyMapper.selectOne(Wrappers.<LikeNotify>lambdaQuery().eq(LikeNotify::getUserId, video.getUserId()));
        if(likeNotify==null){
            likeNotify=new LikeNotify();
            likeNotify.setUserId(ThreadUtils.get());
            notifyMapper.insert(likeNotify);
        }
        likeNotify.setParentId(videoId);
        likeNotify.setParentType(ParentType.video);
        likeNotify.setTime(LocalDateTime.now());
        notifyMapper.up(likeNotify);*/
        //TODO 还得更新通知数量和用户信息里的点赞
        UserInfo data = infoClient.getUserInfo(ThreadUtils.get()).getData();
        data.setLikes(data.getLikes()+1);
        infoClient.updateInfo(data);

        video.setLikes(video.getLikes()+1);
        videoMapper.updateById(video);
    }
}
