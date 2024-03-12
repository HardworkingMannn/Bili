package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.Model.entity.Collection;
import org.example.Model.entity.CollectionContent;

@Mapper
public interface CollectionContentMapper extends BaseMapper<CollectionContent> {
}
