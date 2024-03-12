package org.example.utils;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.provider.symmetric.AES;
import org.example.Model.entity.CoinsAddRecord;
import org.example.Model.entity.UserInfo;
import org.example.mapper.CoinsAddRecordMapper;
import org.example.mapper.DailyMissionMapper;
import org.example.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class DailyUpdater {
    @Autowired
    private DailyMissionMapper dailyMissionMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private CoinsAddRecordMapper coinsAddRecordMapper;
    @XxlJob("updater")
    public void updater(){
        log.info("调用每日更新任务");
        dailyMissionMapper.updateDaily();
        //添加硬币
        userInfoMapper.updateCoins();
        userInfoMapper.selectList(Wrappers.<UserInfo>lambdaQuery().select(UserInfo::getUserId)).stream().map(UserInfo::getUserId).forEach(id->{
            CoinsAddRecord record = new CoinsAddRecord();
            record.setUserId(id);
            record.setCount(1);
            record.setTime(LocalDateTime.now());
            record.setReason("每日奖励");
            coinsAddRecordMapper.insert(record);
        });
    }
}
