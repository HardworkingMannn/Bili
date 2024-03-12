package org.example.service.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.Model.entity.UserInfo;
import org.example.mapper.UserInfoMapper;
import org.example.service.UserInfoService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
}
