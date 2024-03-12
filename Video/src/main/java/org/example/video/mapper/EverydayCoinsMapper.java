package org.example.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.example.Model.entity.EverydayCoins;
import org.example.Model.entity.EverydayLikes;

import java.time.LocalDate;

@Mapper
public interface EverydayCoinsMapper extends BaseMapper<EverydayCoins> {
    @Update("update everyday_coins set everyday_coins = everyday_coins+1 where user_id=#{userId} and date=#{date}")
    public void addEverydayPlays(Integer userId, LocalDate date);
}
