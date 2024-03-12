package org.example.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.function.FailableShortSupplier;
import org.example.Model.entity.Fans;
import org.example.Model.entity.UserInfo;
import org.example.Model.pojo.PageResult;
import org.example.entity.Live;
import org.example.mapper.FansMapper;
import org.example.mapper.LiveMapper;
import org.example.mapper.UserInfoMapper;
import org.example.pojo.*;
import org.example.utils.HttpUtil;
import org.example.video.Model.pojo.Result;
import org.example.video.utils.ThreadUtils;
import org.example.ws.LiveSocket;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/live")
@Slf4j
@Tag(name="直播相关的接口")
public class LiveController {
    @Autowired
    private HttpUtil httpUtil;
    @Autowired
    private LiveMapper liveMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private FansMapper fansMapper;

    @PostMapping("/createLive")
    @Operation(summary = "创建直播间",parameters = {@Parameter(name="type",description = "频道类型。 0:rtmp,1:hls,2:http")})
    public Result<CreateReturn> createLive(@RequestBody CreateLiveDto dto) throws IOException {
        String json = httpUtil.createLive(dto.getName(), dto.getType());
        System.out.println(json);
        RequestResult result = JSON.parseObject(json, RequestResult.class);
        if(result.getCode()==200) {
            CreateLiveVO vo=JSON.parseObject(result.getRet(), CreateLiveVO.class);
            Live live=new Live();
            BeanUtils.copyProperties(vo,live);
            live.setType(dto.getType());
            live.setIsLive(false);
            live.setImage(dto.getCoverImage());
            live.setUserId(ThreadUtils.get());
            liveMapper.insert(live);

            CreateReturn aReturn = new CreateReturn();
            BeanUtils.copyProperties(vo,aReturn);
            return Result.success(aReturn);
        }else{
            log.info("创建直播间失败");
            return Result.fail("创建直播间失败");
        }
    }
    @PostMapping("/reget")
    @Operation(summary = "曾经推拉流失效，重新获取推拉流地址",description = "cid是频道id")
    public Result<RegetVO> reget(String cid) throws IOException {
        String json = httpUtil.reget(cid);
        RequestResult result = JSON.parseObject(json, RequestResult.class);
        if(result.getCode()==200) {
            Object o = result.getRet();
            RegetVO vo = null;
            if (o instanceof RegetVO) {
                vo = (RegetVO) o;
            }
            Live live = liveMapper.selectById(cid);
            live.setPushUrl(vo.getPushUrl());
            live.setHlsPullUrl(vo.getHlsPullUrl());
            live.setHttpPullUrl(vo.getHttpPullUrl());
            live.setRtmpPullUrl(vo.getRtmpPullUrl());
            liveMapper.updateById(live);
            return Result.success(vo);
        }else{
            log.info("重新获取推拉流地址失败");
            return Result.fail("重新获取推拉流地址失败");
        }
    }
    @GetMapping("/enterLive")
    @Operation(summary = "进入直播间")
    public Result<EnterLiveVO> enterLive(String cid){
        EnterLiveVO vo = new EnterLiveVO();
        Live live = liveMapper.selectById(cid);
        BeanUtils.copyProperties(live,vo);
        UserInfo info = userInfoMapper.selectById(live.getUserId());
        BeanUtils.copyProperties(info,vo);
        //统计人数
        vo.setIsFollow(fansMapper.exists(Wrappers.<Fans>lambdaQuery().eq(Fans::getFansId,ThreadUtils.get()).eq(Fans::getUpId,live.getUserId())));
        return Result.success(vo);
    }
    @GetMapping("/onlive")
    @Operation(summary = "开播，返回推流地址")
    public Result<CreateReturn> onlive() throws IOException {
        Integer userId=ThreadUtils.get();
        Live live = liveMapper.selectOne(Wrappers.<Live>lambdaQuery().eq(Live::getUserId,userId));
        if(live==null){
            log.info("直播间不存在,创建直播间");
            //创建直播间
            CreateLiveDto dto = new CreateLiveDto();
            dto.setName(UUID.randomUUID().toString());
            dto.setType(0);
            createLive(dto);
            live = liveMapper.selectOne(Wrappers.<Live>lambdaQuery().eq(Live::getUserId,userId));
        }
        if(live.getUserId()!=userId){
            log.info("没有权限开播别人的直播间");
            return Result.fail("没有权限开播别人的直播间");
        }
        live.setIsLive(true);
        liveMapper.updateById(live);
        //设置开播开始时间
        CreateReturn aReturn = new CreateReturn();
        aReturn.setCid(live.getCid());
        aReturn.setPushUrl(live.getPushUrl());
        return Result.success(aReturn);
    }
    @GetMapping("/offlive")
    @Operation(summary = "下播")
    public Result offlive(){
        Integer userId=ThreadUtils.get();
        Live live = liveMapper.selectOne(Wrappers.<Live>lambdaQuery().eq(Live::getUserId,userId));
        if(live==null){
            log.info("直播间不存在");
            return Result.fail("直播间不存在");
        }
        if(live.getUserId()!=userId){
            log.info("没有权限开播别人的直播间");
            return Result.fail("没有权限开播别人的直播间");
        }
        live.setIsLive(false);
        liveMapper.updateById(live);
        return Result.success();
    }
    @GetMapping("/liveRecommend")
    @Operation(summary = "直播推荐")
    public PageResult<List<LiveOutline>> liveRecommend(Integer pageNum, Integer pageSize){
        Page<Live> page = new Page<>(pageNum,pageSize);
        Page<Live> page1 = liveMapper.selectPage(page, Wrappers.<Live>lambdaQuery().eq(Live::getIsLive, true));
        System.out.println(page1.getPages());
        List<LiveOutline> list = page1.getRecords().stream().map(live -> {
            LiveOutline outline = new LiveOutline();
            BeanUtils.copyProperties(live, outline);
            UserInfo info = userInfoMapper.selectById(live.getUserId());
            BeanUtils.copyProperties(info, outline);
            //todo watch得到websocket里面查找
            outline.setWatch(LiveSocket.playMap.get(live.getCid()));
            Fans fans = fansMapper.selectOne(Wrappers.<Fans>lambdaQuery().eq(Fans::getUpId, info.getUserId()).eq(Fans::getFansId, ThreadUtils.get()));
            if (fans != null) {
                outline.setIsFollowed(true);
            } else {
                outline.setIsFollowed(false);
            }
            return outline;
        }).toList();
        if(pageNum==page1.getPages()){
            return PageResult.success(list, page.getTotal(), pageNum,pageSize,true);
        }
        return PageResult.success(list, page.getTotal(), pageNum,pageSize);
    }
    @GetMapping("/getLivingFollower")
    @Operation(summary = "获取正在直播的关注者")
    public PageResult<List<FollowingLive>> getLivingFollower(Integer pageNum, Integer pageSize){
        List<Fans> fans = fansMapper.selectList(Wrappers.<Fans>lambdaQuery().eq(Fans::getFansId, ThreadUtils.get()));
        List<FollowingLive> list=new ArrayList<>();
        fans.forEach(fan->{
            Live live = liveMapper.selectOne(Wrappers.<Live>lambdaQuery().eq(Live::getUserId, fan.getUpId()).eq(Live::getIsLive, true));
            if(live!=null){
                FollowingLive live1 = new FollowingLive();
                live1.setCid(live.getCid());
                UserInfo info = userInfoMapper.selectById(fan.getUpId());
                BeanUtils.copyProperties(info,live1);
                list.add(live1);
            }
        });
        int start=(pageNum-1)*pageSize;
        int end=pageNum*pageSize;
        double d=list.size()/(double)pageSize;
        int pages;
        if(d%1==0){
            pages=(int)d;
        }else{
            pages=(int)d+1;
        }
        if(pages<pageNum){
            return PageResult.end();
        }
        List<FollowingLive> result = list.subList(start, end);
        if(pages==pageNum){
            return PageResult.success(result,(long)list.size(),pageNum,pageSize,true);
        }
        return PageResult.success(result,(long)list.size(),pageNum,pageSize);
    }
    @GetMapping("/editName")
    @Operation(summary = "修改直播间标题")
    public Result editName(String name){
        Integer userId=ThreadUtils.get();
        Live live = liveMapper.selectOne(Wrappers.<Live>lambdaQuery().eq(Live::getUserId,userId));
        live.setName(name);
        liveMapper.updateById(live);
        return Result.success();
    }
    @GetMapping("/editImage")
    @Operation(summary = "修改直播间封面")
    public Result editImage(String image){
        Integer userId=ThreadUtils.get();
        Live live = liveMapper.selectOne(Wrappers.<Live>lambdaQuery().eq(Live::getUserId,userId));
        live.setImage(image);
        liveMapper.updateById(live);
        return Result.success();
    }
    @GetMapping("/getImage")
    @Operation(summary = "获取封面")
    public Result<String> getImage(){
        Integer userId=ThreadUtils.get();
        Live live = liveMapper.selectOne(Wrappers.<Live>lambdaQuery().eq(Live::getUserId,userId));
        if(live==null){
            log.info("直播间不存在");
            return Result.fail("直播间不存在");
        }
        return Result.success(live.getImage());
    }
    @GetMapping("/getTitle")
    @Operation(summary = "获取标题")
    public Result<String> getTitle(){
        Integer userId=ThreadUtils.get();
        Live live = liveMapper.selectOne(Wrappers.<Live>lambdaQuery().eq(Live::getUserId,userId));
        if(live==null){
            log.info("直播间不存在");
            return Result.fail("直播间不存在");
        }
        return Result.success(live.getName());
    }
    @GetMapping("/getCid")
    @Operation(summary = "获取cid")
    public Result<String> getCid(){
        Integer userId=ThreadUtils.get();
        Live live = liveMapper.selectOne(Wrappers.<Live>lambdaQuery().eq(Live::getUserId,userId));
        if(live==null){
            log.info("直播间不存在");
            return Result.fail("直播间不存在");
        }
        return Result.success(live.getCid());
    }
}
