package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.Model.entity.Fans;

@Mapper
public interface FansMapper extends BaseMapper<Fans> {
}
