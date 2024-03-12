package org.example.utils;

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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name="UserInfoModule")
@Component
public interface UserInfoClient {
    @GetMapping("/info/getUserInfo")
    Result<UserInfo> getUserInfo(@RequestParam("userId") Integer userId);
}
