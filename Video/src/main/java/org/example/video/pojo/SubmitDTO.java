package org.example.video.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "视频投稿")
public class SubmitDTO {
    @Schema(description = "分p的视频链接和分p标题,分P视频的时长（有固定格式hh:mm:ss），之前已经上传过的视频")
    private List<VideoInfo> videos;
    @Schema(description = "封面图片链接，封面图片")
    private String coverImageLink;
    @Schema(description = "视频总标题")
    private String title;
    @Schema(description = "分区")
    private String partitions;
    @Schema(description = "子分区")
    private String subPartition;
    @Schema(description = "标签")
    private List<String> tags;
    private String description;
    @Schema(description = "定时发布时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime releaseTime;
}
