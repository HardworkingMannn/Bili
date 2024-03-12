package com.example.chatroom.utils;

import io.swagger.v3.oas.annotations.Operation;
import org.example.Model.entity.DailyMission;
import org.example.Model.entity.UserInfo;
import org.example.Model.pojo.FansPair;
import org.example.Model.pojo.Pair;
import org.example.video.Model.pojo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="UserInfoModule")
@Component
public interface UserInfoClient {
    @GetMapping("/initDailyMission")
    Result initDailyMission(Integer userId);
}
