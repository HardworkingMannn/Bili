package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.example.Model.entity.DailyMission;
import org.example.Model.entity.UserInfo;

@Mapper
public interface DailyMissionMapper extends BaseMapper<DailyMission> {
    @Update("update daily_mission set daily_reg=false,daily_watch=false,daily_coins=0,daily_share=false")
    public void updateDaily();

}
