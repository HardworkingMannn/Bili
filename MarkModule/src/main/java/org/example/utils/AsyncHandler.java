package org.example.utils;

import lombok.extern.slf4j.Slf4j;
import org.example.Model.entity.CallNotify;
import org.example.Model.pojo.Comment;
import org.example.mapper.CallNotifyMapper;
import org.example.video.utils.ThreadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class AsyncHandler {
    @Autowired
    private CallNotifyMapper callNotifyMapper;
    @Async
    public void addCallNotify(Comment comment, Integer userId){
        log.info("调用异步方法addCallNotify，{}，{}",comment,userId);
        CallNotify callNotify = new CallNotify();
        callNotify.setUserId(userId);
        callNotify.setCallerId(ThreadUtils.get());
        callNotify.setTime(LocalDateTime.now());
        callNotify.setParentId(comment.getParentId());
        callNotify.setFromType(comment.getFromType());
        callNotifyMapper.insert(callNotify);
    }
}
