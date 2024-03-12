package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.Model.entity.Fans;
import org.example.entity.History;

@Mapper
public interface HistoryMapper extends BaseMapper<History> {
}
