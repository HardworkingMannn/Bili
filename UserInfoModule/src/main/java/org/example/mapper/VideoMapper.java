package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.example.Model.entity.Video;

@Mapper
public interface VideoMapper extends BaseMapper<Video> {
    @Update("update video set weight=3*likes+3*plays+5*marks+5*collects+5*coins+5*forwards")
    public void updateAll();
    @Update("update video set weight=0.9*weight")
    void dailyUpdate();
}
