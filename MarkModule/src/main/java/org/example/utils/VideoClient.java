package org.example.utils;

import io.swagger.v3.oas.annotations.Operation;
import org.example.Model.pojo.VideoOutline;
import org.example.video.Model.pojo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(value="Video",path = "/video")
public interface VideoClient {
    @PostMapping("/img")
    Result<String> uploadImg(@RequestPart("file") MultipartFile file);
    @GetMapping("/addMark")
    Result addMark(@RequestParam("videoId") String videoId);
}