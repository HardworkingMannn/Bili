package org.example.Model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("daily_mission")
public class DailyMission {
    @TableId
    private Integer userId;
    private Boolean dailyReg;
    private Boolean dailyWatch;
    private Integer dailyCoins;
    private Boolean dailyShare;
}
