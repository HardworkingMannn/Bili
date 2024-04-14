package org.example.video.service;

import org.example.Model.pojo.*;
import org.example.video.Model.pojo.Result;
import org.example.video.pojo.SubmitDTO;
import org.example.video.pojo.VideoChunkVO;
import org.example.video.pojo.VideoContent;
import org.example.video.pojo.VideoDongtai;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UploadVideoService {
    public Result<VideoChunkVO> uploadChunk(@RequestPart("file") MultipartFile file, int chunkIndex, int chunkTotal, String name, String extension);
    public Result cancelUploadChunk(String name);

    public Result<String> uploadImg(@RequestPart("file") MultipartFile file);

    public Result submit(@RequestBody SubmitDTO dto);

    public Result<VideoContent> getVideo(@PathVariable String videoId);

    public PageResult<List<VideoDongtai>> getVideoDongtai(Integer pageSize, Integer pageNum);

    public VideoOutline getVideoOutline(String videoId);

    Result like(String videoId);

    Result giveCoins(String videoId, Integer count);

    Result addMark(String videoId);

    PageResult<List<VideoOutline>> getLikeVideos(Integer userId, Integer pageNum, Integer pageSize);

    public PageResult<List<VideoOutline>> getCoinsVideos(Integer userId, Integer pageNum, Integer pageSize);

    public PageResult<List<VideoOutline>> getMyVideo(Integer userId, Integer pageNum, Integer pageSize);

    public PageResult<List<VideoOutline>> getPartitionVideo(@RequestBody PartitionVideoDTO dto);

    public Result delVideo(String filename, String extension);

    public Result<DanmuVO> getDanmu(Integer requestId, String videoId, Integer timestamp);
    public Result sendDanmu( SendDanmuVO vo);
}
