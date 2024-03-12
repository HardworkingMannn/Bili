package org.example.video.utils;

import lombok.extern.slf4j.Slf4j;
import org.example.video.constant.RabbitMQConst;
import org.example.Model.entity.Video;
import org.example.video.mapper.VideoMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReleaseVideoHandler {
    @Autowired
    private VideoMapper videoMapper;
    @RabbitListener(queues={RabbitMQConst.DEAD_QUEUE_NAME})
    public void releaseVideo(Long videoId){
        log.info("触发定时发布的消息队列releaseVideo，消息参数为"+videoId);
        Video video = videoMapper.selectById(videoId);
        if(video==null){
            log.info("视频{}已经被提前删除",videoId);
            return;
        }
        if(video.getIsPublished()){
            log.info("视频{}已经被提前发布",videoId);
            return;
        }
        video.setIsPublished(true);
        videoMapper.updateById(video);
    }
}
