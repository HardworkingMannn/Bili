package org.example.video.utils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.example.Model.entity.Video;
import org.example.Model.pojo.PageResult;
import org.example.Model.pojo.VideoOutline;
import org.example.video.mapper.VideoMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class VideoHandler {
    @Autowired
    private VideoMapper videoMapper;
    public final static String WEIGHT_LIST="weightList";
    public final static int COUNT=100;
    public final static int GROUP_COUNT=10;
    public final static int GROUP_SIZE=10;
    public final static int GROUP_END=9;
    @Autowired
    private UserInfoClient infoClient;
    static Jedis jedis = JedisUtil.getJedis();
    @XxlJob("WeightUpdater")
    public void WeightUpdater(){//一小时调用一次
        log.info("调用更新权重任务WeightUpdater");
        videoMapper.updateAll();
        Jedis jedis=null;
        try {
            jedis = JedisUtil.getJedis();
            for (int i = 0; i < GROUP_COUNT; i++) {
                jedis.del(WEIGHT_LIST+i);
            }
            Page<Video> page = new Page<>(0, COUNT);
            Jedis finalJedis = jedis;
            int index=0;
            int count=0;
            List<Video> list = videoMapper.selectPage(page, Wrappers.<Video>lambdaQuery().orderByDesc(Video::getWeight)).getRecords().stream().toList();
            for(Video video:list){
                if(count==GROUP_SIZE){
                    count=0;
                    index++;
                }
                VideoOutline outline = new VideoOutline();
                BeanUtils.copyProperties(video, outline);
                //TODO 还有一个name
                log.info("调用userinfo的getUserInfo接口");
                outline.setName(infoClient.getUserInfo(video.getUserId()).getData().getUserName());
                String jsonString = JSON.toJSONString(outline);
                finalJedis.zadd(WEIGHT_LIST+index, video.getWeight(), jsonString);
                count++;
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }
        log.info("添加完成");
    }
    @XxlJob("DailyWeightUpdater")
    public void DailyWeightUpdater(){//一天调用一次
        log.info("调用更新权重任务DailyWeightUpdater");
        videoMapper.dailyUpdate();
        log.info("调用完成");
    }
    @Transactional
    public List<VideoOutline> getHomePageVideo(Integer pageNum, Integer pageSize,Jedis jedis){
        int start=(pageNum-1)*pageSize;
        int index=start/GROUP_SIZE;
        int indexInGroup=start%GROUP_SIZE;
        List<VideoOutline> list=new ArrayList<>();
        while(list.size()<=pageSize){
            int remain=pageSize-list.size();
            if(remain>GROUP_SIZE-indexInGroup){
                list.addAll(jedis.zrange(WEIGHT_LIST+index,indexInGroup,GROUP_END).stream().map(json->JSON.parseObject(json, VideoOutline.class)).toList());
                indexInGroup=0;
                index++;
            }else{
                list.addAll(jedis.zrange(WEIGHT_LIST+index,indexInGroup,indexInGroup+remain-1).stream().map(json->JSON.parseObject(json, VideoOutline.class)).toList());
            }
        }
        return list;
    }
}
