package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.Model.entity.Mark;
import org.example.Model.entity.Reply;

@Mapper
public interface ReplyMapper extends BaseMapper<Reply> {
}
