package org.example.video.utils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.example.Model.entity.Danmu;
import org.example.Model.entity.DanmuOutline;
import org.example.Model.pojo.LoadIntervalDTO;
import org.example.Model.pojo.SendDanmuVO;
import org.example.video.constant.RabbitMQConst;
import org.example.video.constant.RedisPrefix;
import org.example.video.mapper.DanmuMapper;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.List;

@Component
@Slf4j
public class DanmuLoader {
    @Autowired
    private DanmuMapper danmuMapper;
    @Autowired
    private Jedis jedis;
    @RabbitListener(queuesToDeclare =@Queue(RabbitMQConst.DANMU_LOADER_QUEUE_NAME))
    public void loadInterval(LoadIntervalDTO dto){
        //防止重复查询
        String videoId=dto.getVideoId();
        int backZero=dto.getBackZero();
        for (int i = 0; i < 10; i++) {
            String singlePointKey= RedisPrefix.SINGLE_POINT+videoId+"_"+(backZero+i);
            List<Danmu> danmus = danmuMapper.selectList(Wrappers.<Danmu>lambdaQuery().eq(Danmu::getVideoItemId, videoId).eq(Danmu::getTime, backZero + i));
            String[] list = (String[])(danmus.stream().map(danmu -> {
                DanmuOutline outline = new DanmuOutline();
                BeanUtils.copyProperties(danmu, outline);
                return JSON.toJSONString(outline);
            }).toArray());
            jedis.rpush(singlePointKey,list);
        }
    }
    @RabbitListener(queuesToDeclare =@Queue(RabbitMQConst.DANMU_STORE_QUEUE_NAME))
    public void addDanmu(SendDanmuVO vo){
        Danmu danmu = new Danmu();
        danmu.setTime(vo.getTimestamp());
        danmu.setContent(vo.getContent());
        danmu.setVideoItemId(vo.getVideoId());
        danmuMapper.insert(danmu);
    }
}
