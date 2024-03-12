package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.Model.entity.LikeNotify;
import org.example.Model.entity.ReplyNotify;

@Mapper
public interface ReplyNotifyMapper extends BaseMapper<ReplyNotify> {

}
