package org.example.service.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.Model.entity.DailyMission;
import org.example.Model.entity.UserInfo;
import org.example.mapper.DailyMissionMapper;
import org.example.mapper.UserInfoMapper;
import org.example.service.DailyMissionService;
import org.example.service.UserInfoService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DailyMissionServiceImpl extends ServiceImpl<DailyMissionMapper, DailyMission> implements DailyMissionService {
}
