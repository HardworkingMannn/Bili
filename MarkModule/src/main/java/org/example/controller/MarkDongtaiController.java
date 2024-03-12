package org.example.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.Model.entity.*;
import org.example.Model.pojo.*;
import org.example.Model.constant.MarkType;
import org.example.Model.constant.ParentType;
import org.example.Model.pojo.DongtaiDTO;
import org.example.entity.DongtaiVO;
import org.example.entity.MarkRepresent;
import org.example.mapper.FansMapper;
import org.example.mapper.LikeRecordMapper;
import org.example.mapper.UserInfoMapper;
import org.example.mapper.VideoMapper;
import org.example.pojo.ReplyOutline;
import org.example.service.MarkImageService;
import org.example.service.MarkService;
import org.example.service.ReplyService;
import org.example.video.Model.pojo.Result;
import org.example.video.utils.ThreadUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mark")
@Slf4j
@Tag(name="动态相关接口")
public class MarkDongtaiController {
    @Autowired
    private MarkService markService;
    @Autowired
    private MarkImageService markImageService;
    @Autowired
    private ReplyService replyService;
    @Autowired
    private UserInfoMapper mapper;
    @Autowired
    private VideoMapper videoMapper;
    @Autowired
    private FansMapper fansMapper;
    @Autowired
    private LikeRecordMapper likeRecordMapper;
    @PostMapping("/publishDongtai")
    @Operation(summary = "发表动态")
    public Result publishDongtai(@RequestBody DongtaiDTO dto){
        return markService.publishDongtai(dto);
    }
    @GetMapping("/delete")
    @Operation(summary = "删除动态")
    public Result delete(String dongtaiId){
        Mark id = markService.getById(dongtaiId);
        if(id==null){
            log.info("动态不存在");
            return Result.fail("动态不存在");
        }
        markService.removeById(dongtaiId);
        markImageService.remove(Wrappers.<MarkImage>lambdaQuery().eq(MarkImage::getMarkId,dongtaiId));

        return Result.success();
    }
    @GetMapping("/getDongtai")
    @Operation(summary = "获取动态")
    public PageResult<List<DongtaiVO>> getDongtai(Integer pageNum, Integer pageSize, Integer userId){
        Page<Mark> markPage = new Page<>(pageNum,pageSize);
        Page<Mark> page = markService.page(markPage, Wrappers.<Mark>lambdaQuery().eq(Mark::getUserId, userId)
                .eq(Mark::getMarkType, MarkType.DONGTAI_TYPE).orderByDesc(Mark::getTime));
        if(pageNum>page.getPages()){
            return PageResult.end();
        }
        List<DongtaiVO> vos = page.getRecords().stream().map(mark -> {
            DongtaiVO vo = new DongtaiVO();
            BeanUtils.copyProperties(mark, vo);
            List<String> list = markImageService.list(Wrappers.<MarkImage>lambdaQuery().eq(MarkImage::getMarkId, mark.getMarkId())).stream().map(MarkImage::getMarkImage).toList();
            vo.setImages(list);
            if(vo.getQuoteType()== ParentType.dongtai){
                Mark mark1 = markService.getById(mark.getQuoteId());
                DongtaiOutline outline = new DongtaiOutline();
                BeanUtils.copyProperties(mark1,outline);
                List<String> list1 = markImageService.list(Wrappers.<MarkImage>lambdaQuery().eq(MarkImage::getMarkId, mark1.getMarkId())).stream().map(MarkImage::getMarkImage).toList();
                outline.setImages(list1);

                //设置名字
                UserInfo info = mapper.selectById(mark1.getUserId());
                BeanUtils.copyProperties(info,outline);
                vo.setDongtaiOutline(outline);

                //添加动态的转发
                mark1.setForwards(mark1.getForwards());
                markService.updateById(mark1);
            }else if(vo.getQuoteType()== ParentType.video){
                vo.setVideoOutline(getVideoOutline(mark.getQuoteId()));
            }

            UserInfo info = mapper.selectById(userId);
            vo.setUserId(userId);
            vo.setUserName(info.getUserName());
            vo.setUserImage(info.getUserImage());

            boolean exists = likeRecordMapper.exists(Wrappers.<LikeRecord>lambdaQuery().eq(LikeRecord::getUserId, ThreadUtils.get()).eq(LikeRecord::getMarkId, mark.getMarkId()));
            vo.setIsLike(exists);

            return vo;
        }).toList();
        if(pageNum==page.getPages()){
            return PageResult.success(vos, page.getTotal(), pageNum,pageSize,true);
        }
        return PageResult.success(vos,page.getTotal(),pageNum,pageSize);
    }
    public VideoOutline getVideoOutline(String videoId){//获取视频的概览
        Video video = videoMapper.selectById(videoId);
        if(video==null){
            log.info("{}的视频不存在",videoId);
        }
        VideoOutline outline = new VideoOutline();
        BeanUtils.copyProperties(video,outline);
        //TODO 还有一个name
        log.info("调用userinfo的getUserInfo接口");
        UserInfo info = mapper.selectById(video.getUserId());
        outline.setName(info.getUserName());
        return outline;
    }
    @GetMapping("/getMyFollowsDongtais")
    @Operation(summary = "获取我关注的人的动态")
    public PageResult<List<DongtaiVO>> getMyFollowsDongtais(Integer pageNum, Integer pageSize){
        Integer userId=ThreadUtils.get();
        Page<Mark> markPage = new Page<>(pageNum,pageSize);

        List<Integer> list2 = fansMapper.selectList(Wrappers.<Fans>lambdaQuery().eq(Fans::getFansId, userId)).stream().map(Fans::getUpId).toList();

        Page<Mark> page = markService.page(markPage, Wrappers.<Mark>lambdaQuery().in(Mark::getUserId,list2).or(wrapper->wrapper.eq(Mark::getUserId,userId))
                .eq(Mark::getMarkType, MarkType.DONGTAI_TYPE).orderByDesc(Mark::getTime));
        if(pageNum>page.getPages()){
            return PageResult.end();
        }
        List<DongtaiVO> vos = page.getRecords().stream().map(mark -> {
            DongtaiVO vo = new DongtaiVO();
            BeanUtils.copyProperties(mark, vo);
            List<String> list = markImageService.list(Wrappers.<MarkImage>lambdaQuery().eq(MarkImage::getMarkId, mark.getMarkId())).stream().map(MarkImage::getMarkImage).toList();
            vo.setImages(list);
            if(vo.getQuoteType()== ParentType.dongtai){
                Mark mark1 = markService.getById(mark.getQuoteId());
                DongtaiOutline outline = new DongtaiOutline();
                BeanUtils.copyProperties(mark1,outline);
                List<String> list1 = markImageService.list(Wrappers.<MarkImage>lambdaQuery().eq(MarkImage::getMarkId, mark1.getMarkId())).stream().map(MarkImage::getMarkImage).toList();
                outline.setImages(list1);

                //设置名字
                UserInfo info = mapper.selectById(mark1.getUserId());
                BeanUtils.copyProperties(info,outline);
                vo.setDongtaiOutline(outline);

                //添加动态的转发
                mark1.setForwards(mark1.getForwards());
                markService.updateById(mark1);
            }else if(vo.getQuoteType()== ParentType.video){
                vo.setVideoOutline(getVideoOutline(mark.getQuoteId()));
            }

            UserInfo info = mapper.selectById(mark.getUserId());
            vo.setUserId(userId);
            vo.setUserName(info.getUserName());
            vo.setUserImage(info.getUserImage());

            return vo;
        }).toList();
        if(pageNum==page.getPages()){
            return PageResult.success(vos, page.getTotal(), pageNum,pageSize,true);
        }
        return PageResult.success(vos,page.getTotal(),pageNum,pageSize);
    }
    @PostMapping("/getMarks")
    @Operation(summary = "获取评论",description = "获取的回复也是按热度排序，只有三个",parameters = {@Parameter(name="heat",description = "评论是否按热度排序，否就是时间排序")})
    public PageResult<List<MarkRepresent>> getMarks(@RequestBody GetMarksDTO dto){
        return markService.getReplyMark(dto.getParentId(), dto.getPageNum(), dto.getPageSize(), dto.getHeat());
    }
    @GetMapping("/getReplys")
    @Operation(summary = "获取回复，一般按时间排序")
    public PageResult<List<ReplyOutline>> getReplys(String markId,Integer pageNum, Integer pageSize){
        return markService.getReply(markId, pageNum, pageSize, false);
    }
    @GetMapping("/likeDongtai")
    @Operation(summary = "喜欢动态")
    public Result likeDongtai(String dongtaiId){
        boolean exists = likeRecordMapper.exists(Wrappers.<LikeRecord>lambdaQuery().eq(LikeRecord::getUserId, ThreadUtils.get()).eq(LikeRecord::getMarkId, dongtaiId));
        if(exists){
            return Result.fail("已经喜欢过了");
        }
        mapper.updateLikes(ThreadUtils.get());
        return markService.like(dongtaiId);
    }

    @GetMapping("/likeMark")
    @Operation(summary = "喜欢评论")
    public Result likeMark(String markId){
        return markService.like(markId);
    }
    @GetMapping("/likeReply")
    @Operation(summary = "喜欢回复")
    public Result likeReply(String replyId){
        return markService.likeReply(replyId);
    }
    @PostMapping("/publishMark")
    @Operation(summary = "发表评论")
    public Result publishMark(@RequestBody MarkDTO dto){
        return markService.publishMark(dto);
    }
    @PostMapping("/reply")
    @Operation(summary = "回复评论")
    public Result reply(@RequestBody ReplyDTO dto){
        return markService.reply(dto);
    }
    @GetMapping("/unlikeDongtai")
    @Operation(summary = "不喜欢动态")
    public Result unlikeDongtai(String dongtaiId){
        int delete = likeRecordMapper.delete(Wrappers.<LikeRecord>lambdaQuery().eq(LikeRecord::getUserId, ThreadUtils.get()).eq(LikeRecord::getMarkId, dongtaiId));
        if(delete!=1){
            return  Result.fail("没有喜欢过动态");
        }
        Mark id = markService.getById(dongtaiId);
        id.setLikes(id.getLikes()-1);
        markService.updateById(id);
        return Result.success();
    }
    @GetMapping("/unlikeMark")
    @Operation(summary = "不喜欢评论")
    public Result unlikeMark(String markId){
        int delete = likeRecordMapper.delete(Wrappers.<LikeRecord>lambdaQuery().eq(LikeRecord::getUserId, ThreadUtils.get()).eq(LikeRecord::getMarkId, markId));
        if(delete!=1){
            return  Result.fail("没有喜欢过评论");
        }
        Mark id = markService.getById(markId);
        if(markId==null){
            return  Result.fail("评论不存在");
        }
        id.setLikes(id.getLikes()-1);
        markService.updateById(id);
        return Result.success();
    }
    @GetMapping("/unlikeReply")
    @Operation(summary = "不喜欢回复")
    public Result unlikeReply(String replyId){
        int delete = likeRecordMapper.delete(Wrappers.<LikeRecord>lambdaQuery().eq(LikeRecord::getUserId, ThreadUtils.get()).eq(LikeRecord::getReplyId, replyId));
        if(delete!=1){
            return  Result.fail("没有喜欢过回复");
        }
        Reply reply = replyService.getById(replyId);
        if(reply==null) {
            return Result.fail("恢复不存在");
        }
        reply.setLikes(reply.getLikes()-1);
        replyService.updateById(reply);
        return Result.success();
    }



}
