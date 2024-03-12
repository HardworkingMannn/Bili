package org.example.video.utils;

import io.swagger.v3.oas.annotations.Operation;
import org.example.Model.pojo.DongtaiDTO;
import org.example.video.Model.pojo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("MarkModule")
public interface MarkClient {
    @PostMapping("/publishDongtai")
    public Result publishDongtai(@RequestBody DongtaiDTO dto);
}
