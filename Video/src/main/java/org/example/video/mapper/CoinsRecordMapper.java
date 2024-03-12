package org.example.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.Model.entity.CoinsRecord;
import org.example.Model.entity.LikeRecord;

@Mapper
public interface CoinsRecordMapper extends BaseMapper<CoinsRecord> {
}
