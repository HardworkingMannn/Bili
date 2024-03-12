package org.example.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.video.entity.VideoItem;

@Mapper
public interface VideoItemMapper extends BaseMapper<VideoItem> {
}
