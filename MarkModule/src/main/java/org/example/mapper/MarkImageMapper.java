package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.Model.entity.Mark;
import org.example.Model.entity.MarkImage;

@Mapper
public interface MarkImageMapper extends BaseMapper<MarkImage> {
}
