package org.example.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.Model.entity.LikeRecord;
import org.example.video.entity.Tag;

@Mapper
public interface LikeRecordMapper extends BaseMapper<LikeRecord> {
}