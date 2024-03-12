package org.example.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.example.Model.entity.EverydayCollects;
import org.example.Model.entity.EverydayPlays;

import java.time.LocalDate;

@Mapper
public interface EverydayCollectsMapper extends BaseMapper<EverydayCollects> {
    @Update("update everyday_plays set everyday_plays = everyday_plays+1 where user_id=#{userId} and date=#{date}")
    public void addEverydayPlays(Integer userId, LocalDate date);
}
