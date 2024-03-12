package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.Model.entity.Video;

@Mapper
public interface VideoMapper extends BaseMapper<Video> {
}
