package org.example.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.Model.entity.*;
import org.example.Model.entity.Collection;
import org.example.Model.pojo.*;
import org.example.Model.entity.Collector;
import org.example.entity.CollectorContent;
import org.example.entity.History;
import org.example.mapper.*;
import org.example.pojo.*;
import org.example.service.*;
import org.example.utils.VideoClient;
import org.example.video.Model.pojo.Result;
import org.example.video.utils.ThreadUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@Slf4j
@Tag(name="用户信息相关的接口")
@RequestMapping("/info")
public class UserInfoController {
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private FansService fansService;
    @Autowired
    private VideoClient videoClient;
    @Autowired
    private DailyMissionService missionService;
    @Autowired
    private CollectorService collectorService;
    @Autowired
    private CollectorContentService collectorContentService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private BlacklistMapper blacklistMapper;
    @Autowired
    private CoinsAddRecordMapper addRecordMapper;
    private Map<Integer,Integer> expMap=new HashMap<>();
    @Autowired
    private CollectionMapper collectionMapper;
    @Autowired
    private CollectionContentMapper collectionContentMapper;
    @Autowired
    private VideoMapper videoMapper;
    @PostConstruct
    public void construct(){
        expMap.put(1,200);
        expMap.put(2,1500);
        expMap.put(3,4500);
        expMap.put(4,10800);
        expMap.put(5,28800);
    }
    @GetMapping("/getUserInfo")
    @Operation(summary = "获取用户信息")
    public Result<UserInfo> getUserInfo(@RequestParam(required = false) Integer userId){
        log.info("调用获取用户信息的接口");
        if(userId==null){
            userId=ThreadUtils.get();
            log.info("userId为{}",userId);
        }
        return Result.success(userInfoService.getById(userId));
    }
    @GetMapping("/initDailyMission")
    @Operation(summary = "初始化每日任务，当注册时调用，内部接口")
    public Result initDailyMission(Integer userId){
        DailyMission entity = new DailyMission();
        entity.setUserId(userId);
        missionService.save(entity);
        return Result.success();
    }
    @PostMapping("/editNotice")
    @Operation(summary = "修改公告")
    public Result editNotice(@RequestBody String notice){
        userInfoService.update(Wrappers.<UserInfo>lambdaUpdate().set(UserInfo::getNotice,notice).eq(UserInfo::getUserId,ThreadUtils.get()));
        return Result.success();
    }
    @GetMapping("/getFollowList")
    @Operation(summary = "获得关注的人的信息概览，比如头像之类的")
    public PageResult<UserSimpleInfo> getFollowList(Integer userId,Integer pageNum,Integer pageSize){
        Page<Fans> page = new Page<>(pageNum,pageSize);
        Page<Fans> page1 = fansService.page(page, Wrappers.<Fans>lambdaQuery().eq(Fans::getFansId, userId));
        if(pageNum>page1.getPages()){
            return PageResult.end();
        }
        List<UserSimpleInfo> list = page1.getRecords().stream().map(Fans::getUpId).map(id -> {
            UserInfo id1 = userInfoService.getById(id);
            UserSimpleInfo info = new UserSimpleInfo();
            BeanUtils.copyProperties(id1, info);
            return info;
        }).toList();
        if(pageNum==page1.getPages()){
            return PageResult.success(list, page.getTotal(), pageNum,pageSize,true);
        }
        return PageResult.success(list, page.getTotal(), pageNum,pageSize);
    }
    @GetMapping("/searchName")
    @Operation(summary = "搜索名字")
    public PageResult<UserSimpleInfo> searchName(String name,Integer pageNum,Integer pageSize){
        Page<UserInfo> page = new Page<>(pageNum,pageSize);
        Page<UserInfo> page1 = userInfoService.page(page, Wrappers.<UserInfo>lambdaQuery().select(UserInfo::getUserId, UserInfo::getUserName, UserInfo::getUserImage, UserInfo::getFans).like(UserInfo::getUserName, "%" + name + "%"));
        if(pageNum>page1.getPages()){
            return PageResult.end();
        }
        List<UserSimpleInfo> list = page1.getRecords().stream().map(info1 -> {
            UserSimpleInfo info = new UserSimpleInfo();
            BeanUtils.copyProperties(info1, info);
            return info;
        }).toList();
        if(pageNum==page1.getPages()){
            return PageResult.success(list, page.getTotal(), pageNum,pageSize,true);
        }
        return PageResult.success(list, page.getTotal(), pageNum,pageSize);
    }

    @PostMapping("/isFans")
    @Operation(summary = "是否为粉丝")
    public Result<Boolean> isFans(@RequestBody FansPair fansPair){
        Fans one = fansService.getOne(Wrappers.<Fans>lambdaQuery().eq(Fans::getUpId, fansPair.getUpId()).eq(Fans::getFansId, fansPair.getFansId()));
        return Result.success(one!=null);
    }
    @GetMapping("/getFollowing")
    @Operation(summary = "获取关注的upid，内部调用")
    public Result<List<Integer>> getFollowing(Integer userId){
        log.info("获取关注的upid，内部调用");
        List<Integer> list1 = fansService.list(Wrappers.<Fans>lambdaQuery().eq(Fans::getFansId, userId)).stream().map(Fans::getUpId).toList();
        return Result.success(list1);
    }
    @GetMapping("/follow")
    @Operation(summary = "关注up")
    public Result follow(Integer userId){
        UserInfo id = userInfoService.getById(userId);
        if(id==null){
            return Result.fail("账号不存在");
        }
        Fans entity = new Fans();
        entity.setUpId(userId);
        entity.setFansId(ThreadUtils.get());
        fansService.save(entity);
        return Result.success();
    }
    @GetMapping("/unfollow")
    @Operation(summary = "取关up")
    public Result unfollow(Integer userId){
        UserInfo id = userInfoService.getById(userId);
        if(id==null){
            return Result.fail("账号不存在");
        }
        fansService.remove(Wrappers.<Fans>lambdaQuery().eq(Fans::getFansId,ThreadUtils.get()).eq(Fans::getUpId,userId));
        return Result.success();
    }



    /*@PostMapping("/img")
    @Operation(summary = "上传头像",parameters = {
            @Parameter(name="file",description = "上传的头像文件")
    },
            responses = {
                    @ApiResponse(description = "返回头像图片链接")
            })
    public Result<String> uploadImg(@RequestPart("file") MultipartFile file){
        return videoClient.uploadImg(file);
    }*/

    @PostMapping("/initInfo")
    @Operation(summary = "刚开始登录时的信息上传")
    public Result initInfo(@RequestBody InitInfo info){
        UserInfo id = userInfoService.getById(ThreadUtils.get());
        id.setUserName(info.getUserName());
        id.setUserImage(info.getUserImage());
        userInfoService.updateById(id);
        return Result.success();
    }
    @PostMapping("/addExp")
    @Operation(summary = "添加经验值，内部调用的接口")
    public Result addExp(@RequestBody Pair pair){
        UserInfo info = userInfoService.getById(pair.getUserId());
        info.setExp((int)(info.getExp()+Integer.parseInt(pair.getInfo())));
        userInfoService.updateById(info);
        return Result.success();
    }
    @PostMapping("/updateInfo")
    @Operation(summary = "修改fans，likes等多变数据，内部调用的接口")
    public Result updateInfo(@RequestBody UserInfo info){
        userInfoService.updateById(info);
        return Result.success();
    }

    @GetMapping("/postImg")
    @Operation(summary = "上传头像",parameters = {@Parameter(description = "头像图片链接")})
    public Result postImg(String img){
        UserInfo id = userInfoService.getById(ThreadUtils.get());
        id.setUserImage(img);
        userInfoService.updateById(id);
        return Result.success();
    }
    @PostMapping("/editInfo")
    @Operation(summary = "我的信息修改")
    public Result editInfo(@RequestBody EditInfo info){
        UserInfo id = userInfoService.getById(ThreadUtils.get());
        id.setUserName(info.getUserName());
        id.setSignature(info.getSignature());
        id.setSexType(info.getSexType());
        id.setBirthday(info.getBirthday());
        id.setSchool(info.getSchool());
        userInfoService.updateById(id);
        return Result.success();
    }

    @GetMapping("/accountInfo")
    @Operation(summary = "首页，或者个人信息概览展示")
    public Result<HomePageVO> homePage(){
        HomePageVO vo = new HomePageVO();
        UserInfo id = userInfoService.getById(ThreadUtils.get());
        BeanUtils.copyProperties(id,vo);
        //接下来解决所需经验值的问题
        vo.setNextExp(expMap.get(vo.getLevel()));
        return Result.success(vo);
    }
    @PostMapping("/editDailyMission")
    @Operation(summary = "内部调用的接口，修改每日任务的")
    public Result editDailyMission(@RequestBody DailyMission mission){
        missionService.updateById(mission);
        return Result.success();
    }
    @GetMapping("/getDailyMission")
    @Operation(summary = "获取每日任务，外部调用时用户id不是必要的")
    public Result<DailyMission> getDailyMission(@RequestParam(required = false) Integer userId){
        if(ThreadUtils.get()==null&&userId==null){
            return Result.fail("没有userId");
        }
        if(userId==null){
            userId=ThreadUtils.get();
        }
        return Result.success(missionService.getById(userId));
    }
    @GetMapping("/getCollector")
    @Operation(summary = "获取收藏夹名字")
    public PageResult<List<Collector>> getCollector(Integer userId,Integer pageNum,Integer pageSize){
        Page<Collector> page = new Page<>(pageNum, pageSize);
        Page<Collector> page1 = collectorService.page(page, Wrappers.<Collector>lambdaQuery().select(Collector::getCollectorName, Collector::getCollectorId, Collector::getCount).eq(Collector::getUserId, userId)
                .orderByAsc(Collector::getTime));
        if(pageNum>page1.getPages()){
            return PageResult.end();
        }
        List<Collector> list = page1.getRecords();
        if(pageNum==page1.getPages()){
            return PageResult.success(list, page.getTotal(), pageNum,pageSize,true);
        }
        return PageResult.success(list,page1.getTotal(),pageNum,pageSize);
    }

    @GetMapping("/getCollectorContent")
    @Operation(summary = "获取收藏夹内容")
    public PageResult<List<VideoOutline>> getCollectorContent(String collectorId,Integer pageNum,Integer pageSize){
        Page<CollectorContent> page = new Page<>(pageNum, pageSize);
        Page<CollectorContent> page1 = collectorContentService.page(page, Wrappers.<CollectorContent>lambdaQuery().eq(CollectorContent::getCollectorId, collectorId)
                .orderByAsc(CollectorContent::getTime));
        if(pageNum>page1.getPages()){
            return PageResult.end();
        }
        List<String> list = page1.getRecords().stream().map(CollectorContent::getVideoId).toList();
        List<VideoOutline> data = getVideoOutlines(list).getData();
        if(pageNum==page1.getPages()){
            return PageResult.success(list, page.getTotal(), pageNum,pageSize,true);
        }
        return PageResult.success(data,page1.getTotal(),pageNum,pageSize);
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
        outline.setName(getUserInfo(video.getUserId()).getData().getUserName());
        return outline;
    }
    public Result<List<VideoOutline>> getVideoOutlines(@RequestBody List<String> ids){
        log.info("进入getVideoOutlines接口，参数为：{}",ids);
        List<VideoOutline> list = ids.stream().map(id -> {
            return getVideoOutline(id);
        }).toList();
        return Result.success(list);
    }

    @PostMapping("/collect")
    @Operation(summary = "收藏")
    public Result collect(String collectorId,String videoId){
        CollectorContent content = new CollectorContent();
        content.setCollectorId(collectorId);
        content.setVideoId(videoId);
        content.setTime(LocalDateTime.now());
        collectorContentService.save(content);
        return Result.success();
    }
    @GetMapping("/createCollector")
    @Operation(summary = "创建收藏夹")
    public Result createCollector(String collectorName){
        Collector one = collectorService.getOne(Wrappers.<Collector>lambdaQuery().eq(Collector::getCollectorName, collectorName).eq(Collector::getUserId,ThreadUtils.get()));
        if(one!=null){
            return Result.fail("名字重复");
        }
        Collector collector = new Collector();
        collector.setUserId(ThreadUtils.get());
        collector.setCollectorName(collectorName);
        collector.setTime(LocalDateTime.now());
        collectorService.save(collector);
        return Result.success();
    }
    @PostMapping("/editCollectorName")
    @Operation(summary = "修改收藏夹名字")
    public Result editCollectorName(String collectorId,String collectorName){
        Collector id = collectorService.getById(collectorId);
        if(id==null){
            return Result.fail("没有这个收藏夹");
        }
        collectorService.update(Wrappers.<Collector>lambdaUpdate().set(Collector::getCollectorName,collectorName).eq(Collector::getCollectorId,collectorId));
        return Result.success();
    }
    @PostMapping("/addWatchRecord")
    @Operation(summary = "添加观看记录，内部接口")
    public Result addWatchRecord(@RequestBody Pair pair){
        History history = new History();
        history.setUserId(pair.getUserId());
        history.setVideoId(pair.getInfo());
        history.setTime(LocalDateTime.now());
        historyService.saveOrUpdate(history);
        return Result.success();
    }
    @GetMapping("/getHistory")
    @Operation(summary = "获取记录")
    public PageResult<List<HistoryVO>> getHistory(Integer pageNum,Integer pageSize){
        Page<History> page = new Page<>(pageNum, pageSize);
        Page<History> historyPage = historyService.page(page, Wrappers.<History>lambdaQuery().eq(History::getUserId, ThreadUtils.get()));
        if(pageNum>historyPage.getPages()){
            return PageResult.end();
        }
        List<String> list = historyPage
                .getRecords().stream().map(History::getVideoId).toList();
        if(pageNum==historyPage.getPages()){
            return PageResult.success(getVideoOutlines(list), page.getTotal(), pageNum,pageSize,true);
        }
        return PageResult.success(getVideoOutlines(list).getData(),historyPage.getTotal(),pageNum,pageSize);
    }
    @GetMapping("/getCollectorOutline")
    @Operation(summary="获取我的空间内我的收藏夹")
    public PageResult<List<CollectorOutline>> getCollectorOutline(Integer userId,Integer pageNum,Integer pageSize){
        Page<Collector> page = new Page<>(pageNum, pageSize);
        Page<Collector> page1 = collectorService.page(page, Wrappers.<Collector>lambdaQuery().eq(Collector::getUserId, userId));
        if(pageNum>page1.getPages()){
            return PageResult.end();
        }
        List<CollectorOutline> list = page1.getRecords().stream().map(collector -> {
            CollectorOutline outline = new CollectorOutline();
            BeanUtils.copyProperties(collector, outline);
            String collectorId = collector.getCollectorId();
            CollectorContent one = collectorContentService.list(Wrappers.<CollectorContent>lambdaQuery().eq(CollectorContent::getCollectorId, collectorId).orderByDesc(CollectorContent::getTime)).get(0);
            VideoOutline data = getVideoOutline(one.getVideoId());
            outline.setCoverImageLink(data.getCoverImageLink());
            return outline;
        }).toList();
        if(pageNum==page1.getPages()){
            return PageResult.success(list,page1.getTotal(),pageNum,pageSize,true);
        }
        return PageResult.success(list,page1.getTotal(),pageNum,pageSize);
    }
    @GetMapping("/addBlacklist")
    @Operation(summary = "添加黑名单")
    public Result addBlacklist(Integer userId){
        Blacklist blacklist = new Blacklist();
        blacklist.setUserId(ThreadUtils.get());
        blacklist.setBlockUserId(userId);
        blacklist.setTime(LocalDateTime.now());
        blacklistMapper.insert(blacklist);
        return Result.success();
    }
    @GetMapping("/getBlacklist")
    @Operation(summary = "获取黑名单")
    public PageResult<List<BlacklistVO>> getBlacklist(Integer pageNum, Integer pageSize){//todo 黑名单，不能看到黑名单的视频，评论\
        Page<Blacklist> page = new Page<>(pageNum, pageSize);
        Page<Blacklist> blacklistPage= blacklistMapper.selectPage(page, Wrappers.<Blacklist>lambdaQuery().eq(Blacklist::getUserId, ThreadUtils.get()));
        if(pageNum>blacklistPage.getPages()){
            return PageResult.end();
        }
        List<BlacklistVO> list = blacklistPage.getRecords().stream().map(blacklist -> {
            BlacklistVO blacklist1 = new BlacklistVO();
            UserInfo data = getUserInfo(blacklist.getBlockUserId()).getData();
            BeanUtils.copyProperties(data, blacklist1);
            blacklist1.setTime(blacklist.getTime());
            return blacklist1;
        }).toList();
        if(pageNum==blacklistPage.getPages()){
            return PageResult.success(list,blacklistPage.getTotal(),pageNum,pageSize,true);
        }
        return PageResult.success(list,blacklistPage.getTotal(),pageNum,pageSize);
    }
    @GetMapping("/deleteBlacklist")
    @Operation(summary = "移除黑名单")
    public Result deleteBlacklist(Integer userId){
        blacklistMapper.delete(Wrappers.<Blacklist>lambdaQuery().eq(Blacklist::getBlockUserId,userId));
        return Result.success();
    }
    @GetMapping("/getCoinsAddRecord")
    @Operation(summary = "获取硬币获得情况")
    public PageResult<List<CoinsAddRecord>> getCoinsAddRecord(Integer pageNum, Integer pageSize){
        Page<CoinsAddRecord> page = new Page<>(pageNum, pageSize);
        Page<CoinsAddRecord> page1 = addRecordMapper.selectPage(page, Wrappers.<CoinsAddRecord>lambdaQuery().eq(CoinsAddRecord::getUserId, ThreadUtils.get()).orderByDesc(CoinsAddRecord::getTime));
        if(pageNum>page1.getPages()){
            return PageResult.end();
        }
        if(pageNum==page1.getPages()){
            return PageResult.success(page1.getRecords(),page1.getTotal(),pageNum,pageSize,true);
        }
        return PageResult.success(page1.getRecords(),page1.getTotal(),pageNum,pageSize);
    }
    @GetMapping("/createCollection")
    @Operation(summary = "创建合集")
    public Result createCollection(String name){
        Collection collection = new Collection();
        collection.setCollectionName(name);
        collection.setUserId(ThreadUtils.get());
        collection.setCount(0);
        collection.setTime(LocalDateTime.now());
        collectionMapper.insert(collection);
        return Result.success();
    }
    @GetMapping("/deleteCollection")
    @Operation(summary = "删除合集")
    public Result deleteCollection(String collectionId){
        Collection collection = collectionMapper.selectById(collectionId);
        if(collection==null){
            return Result.fail("合集不存在");
        }
        if(collection.getUserId()!=ThreadUtils.get()){
            return Result.fail("合集不属于本人");
        }
        collectionMapper.deleteById(collectionId);
        return Result.success();
    }
    @GetMapping("/addCollection")
    @Operation(summary = "添加合集")
    public Result addCollection(String collectionId,String videoId){
        Collection collection = collectionMapper.selectById(collectionId);
        if(collection==null){
            return Result.fail("合集不存在");
        }
        if(collection.getUserId()!=ThreadUtils.get()){
            return Result.fail("合集不属于本人");
        }
        collection.setTime(LocalDateTime.now());
        collectionMapper.updateById(collection);

        CollectionContent collection1 = new CollectionContent();
        collection1.setCollectionId(collectionId);
        collection1.setVideoId(videoId);
        collection1.setTime(getVideoOutline(videoId).getReleaseTime());
        collectionContentMapper.insert(collection1);
        return Result.success();
    }
    @GetMapping("/deleteCollectionContent")
    @Operation(summary = "删除合集视频")
    public Result deleteCollectionContent(String collectionId,String videoId){
        Collection collection = collectionMapper.selectById(collectionId);
        if(collection==null){
            return Result.fail("合集不存在");
        }
        if(collection.getUserId()!=ThreadUtils.get()){
            return Result.fail("合集不属于本人");
        }
        collectionContentMapper.delete(Wrappers.<CollectionContent>lambdaQuery().eq(CollectionContent::getCollectionId, collectionId).eq(CollectionContent::getVideoId, videoId));
        return Result.success();
    }
    @GetMapping("/getCollection")
    @Operation(summary = "获取合集视频")
    public PageResult<List<VideoOutline>> getCollection(String collectionId,Integer pageNum,Integer pageSize){
        Collection collection = collectionMapper.selectById(collectionId);
        if(collection==null){
            return PageResult.fail("合集不存在");
        }
        Page<CollectionContent> page = new Page<>(pageNum, pageSize);
        Page<CollectionContent> page1 = collectionContentMapper.selectPage(page, Wrappers.<CollectionContent>lambdaQuery().eq(CollectionContent::getCollectionId, collectionId));
        if(pageNum>page1.getPages()){
            return PageResult.end();
        }
        List<VideoOutline> list = page1.getRecords().stream().map(CollectionContent::getVideoId).map(id -> {
            return getVideoOutline(id);
        }).toList();
        if(pageNum==page1.getPages()){
            return PageResult.success(list,page1.getTotal(),pageNum,pageSize,true);
        }
        return PageResult.success(list,page1.getTotal(),pageNum,pageSize);
    }
    @GetMapping("/getCollectionOutline")
    @Operation(summary = "获取合集外观")
    public PageResult<List<VideoOutline>> getCollectionOutline(@RequestParam(required = false) Integer userId,Integer pageNum,Integer pageSize){
        if(userId==null){
            userId=ThreadUtils.get();
        }
        Page<Collection> page = new Page<>(pageNum , pageSize);
        Page<Collection> page1 = collectionMapper.selectPage(page, Wrappers.<Collection>lambdaQuery().eq(Collection::getUserId, userId).orderByDesc(Collection::getTime));
        if(pageNum>page1.getPages()){
            return PageResult.end();
        }
        List<CollectionOutline> list = page1.getRecords().stream().map(collection -> {
            CollectionOutline outline = new CollectionOutline();
            BeanUtils.copyProperties(collection, outline);
            CollectionContent one = collectionContentMapper.selectOne(Wrappers.<CollectionContent>lambdaQuery().eq(CollectionContent::getCollectionId, collection.getCollectionId()).orderByDesc(CollectionContent::getTime));
            outline.setCoverImageLink(getVideoOutline(one.getVideoId()).getCoverImageLink());
            outline.setTime(one.getTime());
            return outline;
        }).toList();
        if(pageNum==page1.getPages()){
            return PageResult.success(list,page1.getTotal(),pageNum,pageSize,true);
        }
        return PageResult.success(list,page1.getTotal(),pageNum,pageSize);
    }
    @PostMapping("/editSignature")
    @Operation(summary = "修改签名")
    public Result editSignature(String signature){
        userInfoService.update(Wrappers.<UserInfo>lambdaUpdate().set(UserInfo::getSignature,signature).eq(UserInfo::getUserId,ThreadUtils.get()));
        return Result.success();
    }
    @GetMapping("/getFans")
    @Operation(summary = "获取粉丝")
    public PageResult<List<UserSimpleInfo>> getFans(Integer pageNum,Integer pageSize,Integer userId){
        Page<Fans> page = new Page<>(pageNum,pageSize);

        Page<Fans> page1 = fansService.page(page, Wrappers.<Fans>lambdaQuery().eq(Fans::getUpId, userId));
        if(pageNum>page1.getPages()){
            return PageResult.end();
        }
        List<UserSimpleInfo> list = page1.getRecords().stream().map(fans -> {
            UserInfo id = userInfoService.getById(fans.getFansId());
            UserSimpleInfo info = new UserSimpleInfo();
            BeanUtils.copyProperties(id, info);
            info.setIsFollow(isFans(new FansPair(info.getUserId(),userId)).getData());
            return info;
        }).toList();
        if(pageNum==page.getPages()){
            return PageResult.success(list, page.getTotal(), pageNum,pageSize,true);
        }
        return PageResult.success(list,page.getTotal(),pageNum,pageSize);

    }
}
