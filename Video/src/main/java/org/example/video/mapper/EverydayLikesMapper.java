package org.example.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.example.Model.entity.EverydayLikes;
import org.example.Model.entity.EverydayPlays;

import java.time.LocalDate;

@Mapper
public interface EverydayLikesMapper extends BaseMapper<EverydayLikes> {
    @Update("update everyday_likes set everyday_likes = everyday_likes+1 where user_id=#{userId} and date=#{date}")
    public void addEverydayPlays(Integer userId, LocalDate date);
}
