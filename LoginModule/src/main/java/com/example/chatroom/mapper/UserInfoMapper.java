package com.example.chatroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.example.Model.entity.UserInfo;
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
    @Update("update user_info set coins=coins+1")
    void updateCoins();
}
