package org.example.service.serviceImpl;

import com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.internal.StringUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.Model.constant.LikeNotifyConst;
import org.example.Model.entity.*;
import org.example.Model.pojo.CallHandler;
import org.example.Model.pojo.MarkDTO;
import org.example.Model.pojo.PageResult;
import org.example.Model.pojo.Record;
import org.example.Model.pojo.ReplyDTO;
import org.example.aop.CallAdvice;
import org.example.Model.constant.MarkType;
import org.example.Model.constant.ParentType;
import org.example.Model.pojo.DongtaiDTO;
import org.example.entity.MarkRepresent;
import org.example.mapper.*;
import org.example.pojo.ReplyOutline;
import org.example.service.MarkImageService;
import org.example.service.MarkService;
import org.example.utils.UserInfoClient;
import org.example.utils.VideoClient;
import org.example.video.Model.pojo.Result;
import org.example.video.utils.ThreadUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MarkServiceImpl extends ServiceImpl<MarkMapper, Mark> implements MarkService {
    @Autowired
    private MarkMapper markMapper;
    @Autowired
    private MarkImageMapper markImageMapper;
    @Autowired
    private ReplyMapper replyMapper;
    @Autowired
    private UserInfoClient infoClient;
    @Autowired
    private VideoClient videoClient;
    @Autowired
    private LikeNotifyMapper notifyMapper;
    @Autowired
    private ReplyNotifyMapper replyNotifyMapper;
    @Autowired
    private MarkImageService markImageService;
    @Autowired
    private UserInfoMapper mapper;
    @Autowired
    private CallAdvice callAdvice;
    @Autowired
    private LikeRecordMapper likeRecordMapper;
    public Result publishDongtai(DongtaiDTO dto){
        Mark mark = new Mark();
        mark.setUserId(ThreadUtils.get());
        mark.setMarkType(MarkType.DONGTAI_TYPE);
        mark.setContent(dto.getContent());
        mark.setTime(LocalDateTime.now());

        mark.setQuoteId(dto.getQuoteId());
        mark.setQuoteType(dto.getQuoteType());

        //发送@功能
        save(mark);
        dto.setParentId(mark.getMarkId());
        dto.setFromType(ParentType.dongtai);
        callAdvice.handle(dto.getContent(),dto);
        if(dto.getImages()!=null&&dto.getImages().size()!=0) {
            dto.getImages().forEach(image -> {
                MarkImage markImage = new MarkImage();
                if (StringUtil.isNullOrEmpty(image)) {
                    return;
                }
                markImage.setMarkImage(image);
                markImage.setMarkId(mark.getMarkId());
                markImageService.save(markImage);
            });
        }

        //添加动态数量
        mapper.updateDongtais(ThreadUtils.get());
        return Result.success();
    }
    public Result like(String markId){
        Mark mark = markMapper.selectById(markId);
        mark.setLikes(mark.getLikes()+1);
        markMapper.updateById(mark);

        //todo 还得把用户获得赞的总数添加，还有通知
        LikeNotify likeNotify = notifyMapper.selectOne(Wrappers.<LikeNotify>lambdaQuery().eq(LikeNotify::getUserId, mark.getUserId()).eq(LikeNotify::getReplyId, markId));
        if(likeNotify!=null) {
            likeNotify.setTime(LocalDateTime.now());
            likeNotify.setLikeType(LikeNotifyConst.MARK_LIKE);
            if(mark.getMarkType()==MarkType.DONGTAI_TYPE){
                likeNotify.setParentId(mark.getMarkId());
                likeNotify.setParentType(ParentType.dongtai);
                likeNotify.setMarkType(MarkType.DONGTAI_TYPE);
            }else{
                likeNotify.setParentId(mark.getParentId());
                likeNotify.setParentType(mark.getParentType());
                likeNotify.setMarkType(MarkType.MARK_TYPE);
            }
            likeNotify.setMarkId(mark.getMarkId());
            notifyMapper.updateById(likeNotify);
        }else{
            likeNotify=new LikeNotify();
            likeNotify.setUserId(mark.getUserId());
            likeNotify.setMarkId(mark.getMarkId());
            likeNotify.setParentId(mark.getParentId());
            likeNotify.setParentType(mark.getParentType());
            likeNotify.setLikeType(LikeNotifyConst.MARK_LIKE);
            likeNotify.setTime(LocalDateTime.now());
            notifyMapper.insert(likeNotify);
        }

        LikeRecord record = new LikeRecord();
        record.setUserId(ThreadUtils.get());
        record.setMarkId(mark.getMarkId());
        record.setTime(LocalDateTime.now());
        likeRecordMapper.insert(record);

        return Result.success();
    }
    public Result likeReply(String replyId){
        Reply reply = replyMapper.selectById(replyId);
        reply.setLikes(reply.getLikes()+1);
        replyMapper.updateById(reply);

        //更新通知：
        LikeNotify likeNotify = notifyMapper.selectOne(Wrappers.<LikeNotify>lambdaQuery().eq(LikeNotify::getUserId, reply.getUserId()).eq(LikeNotify::getReplyId, replyId));
        if(likeNotify!=null) {
            likeNotify.setTime(LocalDateTime.now());
            likeNotify.setParentId(reply.getParentId());
            likeNotify.setParentType(reply.getParentType());
            likeNotify.setMarkId(reply.getMarkId());
            likeNotify.setReplyId(replyId);
            likeNotify.setLikeType(LikeNotifyConst.REPLY_LIKE);
            notifyMapper.update(likeNotify,Wrappers.<LikeNotify>lambdaQuery().eq(LikeNotify::getUserId, reply.getUserId()).eq(LikeNotify::getReplyId, replyId));
        }else{
            likeNotify=new LikeNotify();
            likeNotify.setUserId(reply.getUserId());
            likeNotify.setMarkId(reply.getMarkId());
            likeNotify.setParentId(reply.getParentId());
            likeNotify.setParentType(reply.getParentType());
            likeNotify.setReplyId(replyId);
            likeNotify.setLikeType(LikeNotifyConst.REPLY_LIKE);
            likeNotify.setTime(LocalDateTime.now());
            notifyMapper.insert(likeNotify);
        }

        LikeRecord record = new LikeRecord();
        record.setUserId(ThreadUtils.get());
        record.setReplyId(replyId);
        record.setTime(LocalDateTime.now());
        likeRecordMapper.insert(record);

        return Result.success();
    }
    public void addMark(String markId){
        Mark mark = markMapper.selectById(markId);
        mark.setMarks(mark.getMarks()+1);
        markMapper.updateById(mark);
    }
    public PageResult<List<MarkRepresent>> getReplyMark(String id, Integer pageNum, Integer pageSize, boolean heat){//可能是动态的评论，也可能是视频的评论，是否是按热度排序
        Page<Mark> page = new Page<>(pageNum, pageSize);
        Page<Mark> page1 = markMapper.selectPage(page, Wrappers.<Mark>lambdaQuery().eq(Mark::getParentId, id)
                .eq(Mark::getMarkType, MarkType.MARK_TYPE).orderByDesc(heat, Mark::getWeight).orderByDesc(Mark::getTime));
        if(page1.getPages()<pageNum){
            return PageResult.end();
        }
        List<MarkRepresent> list = page1.getRecords().stream().map(mark -> {
            MarkRepresent represent = new MarkRepresent();
            BeanUtils.copyProperties(mark, represent);
            List<String> images = getMarkImage(mark.getMarkId());
            represent.setImages(images);
            UserInfo data = infoClient.getUserInfo(mark.getUserId()).getData();
            represent.setUserName(data.getUserName());
            represent.setUserImage(data.getUserImage());
            represent.setLevel(data.getLevel());
            Record<List<ReplyOutline>> data1 = getReply(mark.getMarkId(), 1, 3, true).getData();
            represent.setReplys(data1==null?null:data1.getRecord());

            boolean exists = likeRecordMapper.exists(Wrappers.<LikeRecord>lambdaQuery().eq(LikeRecord::getUserId, ThreadUtils.get()).eq(LikeRecord::getMarkId, mark.getMarkId()));
            represent.setIsLike(exists);

            return represent;
        }).toList();
        if(pageNum==page1.getPages()){
            return PageResult.success(list,page1.getTotal(),pageNum,pageSize,true);
        }
        return PageResult.success(list,page1.getTotal(),pageNum,pageSize);
    }
    public List<String> getMarkImage(String markId){
        return markImageMapper.selectList(Wrappers.<MarkImage>lambdaQuery()
                .eq(MarkImage::getMarkId, markId)).stream().map(MarkImage::getMarkImage).toList();

    }
    public PageResult<List<ReplyOutline>> getReply(String markId,Integer pageNum,Integer pageSize,boolean like){
        Page<Reply> replyPage = new Page<>(pageNum , pageSize);
        Page<Reply> page = replyMapper.selectPage(replyPage, Wrappers.<Reply>lambdaQuery().eq(Reply::getMarkId, markId)
                .orderByDesc(!like, Reply::getLikes).orderByAsc(Reply::getTime));
        if(page.getPages()<pageNum){
            return PageResult.end();
        }
        List<ReplyOutline> list = page.getRecords().stream().map(reply -> {
            ReplyOutline outline = new ReplyOutline();
            BeanUtils.copyProperties(reply, outline);
            UserInfo data = infoClient.getUserInfo(reply.getUserId()).getData();
            outline.setUserName(data.getUserName());
            outline.setUserImage(data.getUserImage());
            outline.setLevel(data.getLevel());

            boolean exists = likeRecordMapper.exists(Wrappers.<LikeRecord>lambdaQuery().eq(LikeRecord::getUserId, ThreadUtils.get()).eq(LikeRecord::getReplyId, reply.getReplyId()));
            outline.setIsLike(exists);

            return outline;
        }).toList();
        if(pageNum==page.getPages()){
            return PageResult.success(list,page.getTotal(),pageNum,pageSize,true);
        }
        return PageResult.success(list,page.getTotal(),pageNum,pageSize);
    }
    @CallHandler
    public Result publishMark(MarkDTO dto){
        String parentId=dto.getParentId();
        Mark mark = new Mark();
        mark.setUserId(ThreadUtils.get());
        if(dto.getFromType()==ParentType.dongtai){
            mark.setParentId(parentId);
            mark.setParentType(ParentType.dongtai);
            //给动态添加评论数
            addMark(parentId);
        }else{
            mark.setParentId(parentId);
            mark.setParentType(ParentType.video);
            //todo 视频添加评论数
            videoClient.addMark(parentId);
        }
        mark.setMarkType(MarkType.MARK_TYPE);
        mark.setContent(dto.getContent());
        mark.setTime(LocalDateTime.now());
        markMapper.insert(mark);
        if(dto.getImages()!=null) {
            for (String image : dto.getImages()) {
                MarkImage markImage = new MarkImage();
                markImage.setMarkId(mark.getMarkId());
                markImage.setMarkImage(image);
                markImageMapper.insert(markImage);
            }
        }

        //添加回复通知
        ReplyNotify notify = new ReplyNotify();
        notify.setUserId(dto.getAuthorId());
        notify.setFromType(dto.getFromType()==MarkType.DONGTAI_TYPE?MarkType.DONGTAI_TYPE:MarkType.MARK_TYPE);
        notify.setParentId(dto.getParentId());
        if(dto.getFromType()==MarkType.DONGTAI_TYPE) {
            Mark id = markMapper.selectById(parentId);
            notify.setContent(id.getContent());
        }
        notify.setReplyContent(dto.getContent());
        notify.setReplyerId(ThreadUtils.get());
        notify.setTime(LocalDateTime.now());
        notify.setMarkId(dto.getMarkId());
        replyNotifyMapper.insert(notify);
        //TODO 用websocket通知有消息

        return Result.success();
    }

    @Override
    @CallHandler
    public Result reply(ReplyDTO dto) {
        Reply reply = new Reply();
        BeanUtils.copyProperties(dto,reply);
        reply.setTime(LocalDateTime.now());
        replyMapper.insert(reply);

        addMark(dto.getMarkId());

        ReplyNotify notify = new ReplyNotify();
        if(dto.getReplyId()!=null) {
            Reply reply1 = replyMapper.selectById(dto.getReplyId());
            notify.setUserId(reply1.getUserId());
            notify.setContent(reply1.getContent());
        }else{
            Mark mark = markMapper.selectById(dto.getMarkId());
            notify.setUserId(mark.getUserId());
            notify.setContent(mark.getContent());
        }
        notify.setParentId(dto.getParentId());
        notify.setFromType(dto.getFromType());

        notify.setReplyContent(dto.getContent());
        notify.setReplyerId(ThreadUtils.get());
        notify.setMarkId(dto.getReplyId());
        notify.setTime(LocalDateTime.now());
        replyNotifyMapper.insert(notify);
        //todo 回复通知
        return Result.success();
    }



}
