package org.example.video.utils;

import io.swagger.v3.oas.annotations.Operation;
import org.example.Model.entity.DailyMission;
import org.example.Model.entity.UserInfo;
import org.example.Model.pojo.FansPair;
import org.example.Model.pojo.Pair;
import org.example.video.Model.pojo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value="UserInfoModule")
public interface UserInfoClient {
    @GetMapping(value="/info/getUserInfo")
    Result<UserInfo> getUserInfo(@RequestParam("userId") Integer userId);
    @PostMapping("/info/isFans")
     Result<Boolean> isFans(@RequestBody FansPair fansPair);
    @PostMapping("/info/editDailyMission")
    Result editDailyMission(@RequestBody DailyMission mission);
    @GetMapping("/info/getDailyMission")
    Result<DailyMission> getDailyMission(@RequestParam("userId")Integer userId);
    @PostMapping("/info/addExp")
    Result addExp(@RequestBody Pair pair);
    @PostMapping("/info/updateInfo")
    Result updateInfo(@RequestBody UserInfo info);
    @GetMapping("/info/getFollowing")
    Result<List<Integer>> getFollowing(@RequestParam("userId") Integer userId);
}
