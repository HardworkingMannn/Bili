package com.example.gateway;

import org.example.Model.entity.DailyMission;
import org.example.video.Model.pojo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="UserInfoModule")
@Component
public interface UserInfoClient {
    @PostMapping("/editDailyMission")
    Result editDailyMission(@RequestBody DailyMission mission);
    @GetMapping("/getDailyMission")
    Result<DailyMission> getDailyMission(Integer userId);
}
