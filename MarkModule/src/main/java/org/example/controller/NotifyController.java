package org.example.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.provider.symmetric.AES;
import org.example.Model.constant.ParentType;
import org.example.Model.entity.ReplyNotify;
import org.example.Model.entity.UserInfo;
import org.example.Model.entity.Video;
import org.example.Model.pojo.*;
import org.example.mapper.ReplyNotifyMapper;
import org.example.mapper.UserInfoMapper;
import org.example.mapper.VideoMapper;
import org.example.service.NotifyService;
import org.example.video.Model.pojo.Result;
import org.example.video.utils.ThreadUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/mark/notify")
@Tag(name="通知相关的接口")
public class NotifyController {
    @Autowired
    private NotifyService notifyService;
    @Autowired
    private ReplyNotifyMapper replyNotifyMapper;
    @Autowired
    private UserInfoMapper infoMapper;
    @Autowired
    private VideoMapper videoMapper;
    @GetMapping("/getLikes")
    public  Result<List<LikeNotifyOutline>> getLikes(Integer pageNum, Integer pageSize){
        return notifyService.getLikeNotify(pageNum, pageSize);
    }
    @GetMapping("/getLikeDetails")
    @Operation(description = "获取点赞详情",parameters = {@Parameter(name="id",description = "id，视频id或者评论id，回复id，根据类型不同传不同id"),
    @Parameter(name="likeType",description = "类型，1为视频，2为评论，3为回复")})
    public Result<List<LikeDetails>> getLikeDetails(Integer pageNum, Integer pageSize, String id, Integer likeType){
        return notifyService.getLikeDetails(pageNum, pageSize, id, likeType);
    }
    @GetMapping("/getReplyNotify")
    @Operation(description = "获取回复通知")
    public Result<List<ReplyNotifyVO>> getReplyNotify(Integer pageNum, Integer pageSize){
        return notifyService.getReplyNotify(pageNum, pageSize);
    }
    @GetMapping("/getCall")
    @Operation(summary = "获取@通知")
    public Result getCall(Integer pageNum, Integer pageSize){
        return notifyService.getCall(pageNum, pageSize);
    }
    @GetMapping("/markManagement")
    @Operation(summary = "评论管理",parameters = {@Parameter(description = "1为按时间，2为按点赞，3为按评论",name="type")})
    public PageResult<List<MarkManagement>> markManagement(Integer pageNum, Integer pageSize,Integer type){
        Page<ReplyNotify> page = new Page<>(pageNum, pageSize);
        Page<ReplyNotify> page1 = replyNotifyMapper.selectPage(page, Wrappers.<ReplyNotify>lambdaQuery().eq(ReplyNotify::getUserId, ThreadUtils.get()).eq(ReplyNotify::getFromType,2)
                .orderByDesc(type==1,ReplyNotify::getTime).orderByDesc(type==2,ReplyNotify::getLikes).orderByDesc(type==3,ReplyNotify::getMarks));
        if(pageNum>page1.getPages()){
            return PageResult.end();
        }
        List<MarkManagement> list = page1.getRecords().stream().map(notify -> {
            MarkManagement management = new MarkManagement();
            BeanUtils.copyProperties(notify, management);
            Integer id = management.getUserId();
            UserInfo info = infoMapper.selectById(id);
            management.setUserName(info.getUserName());
            management.setUserImage(info.getUserImage());
            if(notify.getFromType()== ParentType.video){
                management.setVideoId(notify.getParentId());
                Video video = videoMapper.selectById(management.getVideoId());
                management.setCoverImageLink(video.getCoverImageLink());
                management.setTitle(video.getTitle());
            }
            return management;
        }).toList();
        if(pageNum==page1.getPages()){
            return PageResult.success(list,page1.getTotal(),pageNum,pageSize,true);
        }
        return PageResult.success(list,page1.getTotal(),pageNum,pageSize);
    }
    @GetMapping("/delMark")
    @Operation(summary = "删除评论")
    public Result delMark(String markId){
        replyNotifyMapper.delete(Wrappers.<ReplyNotify>lambdaQuery().eq(ReplyNotify::getMarkId,markId));
        return Result.success();
    }
}
