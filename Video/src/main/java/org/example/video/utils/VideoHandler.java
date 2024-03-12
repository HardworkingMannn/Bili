package org.example.video.utils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.example.Model.entity.Video;
import org.example.Model.pojo.VideoOutline;
import org.example.video.mapper.VideoMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.List;

@Component
@Slf4j
public class VideoHandler {
    @Autowired
    private VideoMapper videoMapper;
    public final static String WEIGHT_LIST="weightList";
    public final static int COUNT=100;
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
            jedis.del(WEIGHT_LIST);
            Page<Video> page = new Page<>(0, COUNT);
            Jedis finalJedis = jedis;
            videoMapper.selectPage(page, Wrappers.<Video>lambdaQuery().orderByDesc(Video::getWeight)).getRecords().stream().forEach(video -> {
                VideoOutline outline = new VideoOutline();
                BeanUtils.copyProperties(video, outline);
                //TODO 还有一个name
                log.info("调用userinfo的getUserInfo接口");
                outline.setName(infoClient.getUserInfo(video.getUserId()).getData().getUserName());
                String jsonString = JSON.toJSONString(outline);
                finalJedis.zadd(WEIGHT_LIST, video.getWeight(), jsonString);
            });
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
}
