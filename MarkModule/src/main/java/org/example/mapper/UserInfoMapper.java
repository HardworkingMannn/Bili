package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.example.Model.entity.UserInfo;

@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
    @Update("update user_info set coins=coins+1 where user_id=#{userId}")
    void updateCoins(Integer userId);
    @Update("update user_info set likes=likes+1 where user_id=#{userId}")
    void updateLikes(Integer userId);
    @Update("update user_info set likes=likes-1 where user_id=#{userId}")
    void delLikes(Integer userId);
    @Update("update user_info set dongtais=dongtais+1 where user_id=#{userId}")
    void updateDongtais(Integer userId);


}
