package org.example.service.serviceImpl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.Model.constant.LikeNotifyConst;
import org.example.Model.entity.*;
import org.example.Model.pojo.*;
import org.example.mapper.*;
import org.example.service.MarkService;
import org.example.service.NotifyService;
import org.example.service.ReplyService;
import org.example.utils.UserInfoClient;
import org.example.video.Model.pojo.Result;
import org.example.video.utils.ThreadUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotifyServiceImpl implements NotifyService {
    @Autowired
    private LikeNotifyMapper likeNotifyMapper;
    @Autowired
    private LikeRecordMapper likeRecordMapper;
    @Autowired
    private UserInfoClient infoClient;
    @Autowired
    private MarkService markService;
    @Autowired
    private ReplyService replyService;
    @Autowired
    private VideoMapper videoMapper;
    @Autowired
    private ReplyNotifyMapper replyNotifyMapper;
    @Autowired
    private CallNotifyMapper callNotifyMapper;
    @Autowired
    private UserInfoMapper infoMapper;
    public Result<List<LikeNotifyOutline>> getLikeNotify(Integer pageNum, Integer pageSize){
        Page<LikeNotify> page = new Page<>(pageNum, pageSize);
        List<LikeNotifyOutline> list1 = likeNotifyMapper.selectPage(page, Wrappers.<LikeNotify>lambdaQuery().eq(LikeNotify::getUserId, ThreadUtils.get()).orderByDesc(LikeNotify::getTime)).getRecords().stream().map(likeNotify -> {
            LikeNotifyOutline outline = new LikeNotifyOutline();
            BeanUtils.copyProperties(likeNotify, outline);
            //获取点赞的头两个用户信息
            Page<LikeRecord> page1 = new Page<>(1, 2);
            List<UserSimpleInfo> list = likeRecordMapper.selectPage(page1, Wrappers.<LikeRecord>lambdaQuery().eq
                    (LikeNotifyConst.VIDEO_LIKE == likeNotify.getLikeType(), LikeRecord::getVideoId, likeNotify.getParentId()).or(wrapper->
                    wrapper.eq(LikeNotifyConst.MARK_LIKE == likeNotify.getLikeType(), LikeRecord::getMarkId, likeNotify.getMarkId()).or(wrapper1->
                                    wrapper1.eq(LikeNotifyConst.REPLY_LIKE == likeNotify.getLikeType(), LikeRecord::getReplyId, likeNotify.getReplyId()))
                    )
                    .orderByDesc(LikeRecord::getTime)).getRecords().stream().map(likeRecord -> {
                UserInfo userInfo = infoClient.getUserInfo(likeRecord.getUserId()).getData();
                UserSimpleInfo info = new UserSimpleInfo();
                BeanUtils.copyProperties(userInfo, info);
                return info;
            }).toList();
            outline.setInfos(list);

            //获得点赞的内容和点赞数量
            if (LikeNotifyConst.VIDEO_LIKE == likeNotify.getLikeType()) {
                //获取视频的
                Video video = videoMapper.selectById(likeNotify.getParentId());
                outline.setContent(video.getCoverImageLink());
                outline.setLikes(video.getLikes());
            } else if (LikeNotifyConst.MARK_LIKE == likeNotify.getLikeType()) {
                Mark mark = markService.getById(likeNotify.getMarkId());
                outline.setLikes(mark.getLikes());
                outline.setContent(mark.getContent());
            } else {
                Reply reply = replyService.getById(likeNotify.getReplyId());
                outline.setLikes(reply.getLikes());
                outline.setContent(reply.getContent());
            }
            return outline;
        }).toList();
        return Result.success(list1);
    }
    public Result<List<LikeDetails>> getLikeDetails(Integer pageNum, Integer pageSize,String id,Integer likeType){
        Page<LikeRecord> page = new Page<>(pageNum, pageSize);
        if (LikeNotifyConst.VIDEO_LIKE ==likeType) {
            //获取视频的
            List<LikeDetails> list = likeRecordMapper.selectPage(page, Wrappers.<LikeRecord>lambdaQuery().eq(LikeRecord::getVideoId, id).orderByDesc(LikeRecord::getTime)).getRecords().stream().map(likeRecord -> {
                LikeDetails details = new LikeDetails();
                details.setTime(likeRecord.getTime());
                UserInfo data = infoClient.getUserInfo(likeRecord.getUserId()).getData();
                UserSimpleInfo info = new UserSimpleInfo();
                BeanUtils.copyProperties(data,info);
                details.setInfo(info);
                return details;
            }).toList();
            return Result.success(list);
        } else if (LikeNotifyConst.MARK_LIKE == likeType) {
            List<LikeDetails> list = likeRecordMapper.selectPage(page, Wrappers.<LikeRecord>lambdaQuery().eq(LikeRecord::getMarkId, id).orderByDesc(LikeRecord::getTime)).getRecords().stream().map(likeRecord -> {
                LikeDetails details = new LikeDetails();
                details.setTime(likeRecord.getTime());
                UserInfo data = infoClient.getUserInfo(likeRecord.getUserId()).getData();
                UserSimpleInfo info = new UserSimpleInfo();
                BeanUtils.copyProperties(data,info);
                details.setInfo(info);
                return details;
            }).toList();
            return Result.success(list);

        } else {
            List<LikeDetails> list = likeRecordMapper.selectPage(page, Wrappers.<LikeRecord>lambdaQuery().eq(LikeRecord::getReplyId, id).orderByDesc(LikeRecord::getTime)).getRecords().stream().map(likeRecord -> {
                LikeDetails details = new LikeDetails();
                details.setTime(likeRecord.getTime());
                UserInfo data = infoClient.getUserInfo(likeRecord.getUserId()).getData();
                UserSimpleInfo info = new UserSimpleInfo();
                BeanUtils.copyProperties(data,info);
                details.setInfo(info);
                return details;
            }).toList();
            return Result.success(list);
        }
    }
    public Result<List<ReplyNotifyVO>> getReplyNotify(Integer pageNum,Integer pageSize){
        Page<ReplyNotify> page = new Page<>(pageNum , pageSize);
        List<ReplyNotifyVO> list = replyNotifyMapper.selectPage(page, Wrappers.<ReplyNotify>lambdaQuery().eq(ReplyNotify::getUserId, ThreadUtils.get()).orderByDesc(ReplyNotify::getTime)).getRecords().stream().map(replyNotify -> {
            ReplyNotifyVO vo = new ReplyNotifyVO();
            BeanUtils.copyProperties(replyNotify, vo);
            UserInfo data = infoClient.getUserInfo(replyNotify.getReplyerId()).getData();
            UserSimpleInfo info = new UserSimpleInfo();
            BeanUtils.copyProperties(data, info);
            vo.setInfo(info);
            return vo;
        }).toList();
        return Result.success(list);
    }

    @Override
    public Result getCall(Integer pageNum, Integer pageSize) {
        Page<CallNotify> page = new Page<>(pageNum, pageSize);
        List<CallNotifyVO> list = callNotifyMapper.selectPage(page, Wrappers.<CallNotify>lambdaQuery().eq(CallNotify::getUserId, ThreadUtils.get())).getRecords().stream().map(callNotify -> {
            CallNotifyVO vo = new CallNotifyVO();
            BeanUtils.copyProperties(callNotify, vo);
            vo.setUserId(callNotify.getCallerId());
            UserInfo info = infoMapper.selectById(callNotify.getCallerId());
            vo.setUserName(info.getUserName());
            vo.setUserImage(info.getUserImage());
            return vo;
        }).toList();
        return Result.success(list);
    }

}
