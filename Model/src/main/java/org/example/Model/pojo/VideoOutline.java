package org.example.Model.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data

public class VideoOutline {
    @Schema(description = "视频id，通过这个访问视频")
    private String videoId;
    @Schema(description = "封面")
    private String coverImageLink;
    @Schema(description = "视频i标题")
    private String title;
    @Schema(description = "播放量")
    private Integer plays;  //播放量
    @Schema(description = "视频标题")
    private String description;
    @Schema(description = "评论数")
    private Integer marks;  //评论数
    //总时长
    @Schema(description = "多少小时")
    private Integer totalHours;
    @Schema(description = "多少分钟")
    private Integer totalMinutes;
    @Schema(description = "多少秒")
    private Integer totalSeconds;

    @Schema(description = "作者名字")
    private String name;
    @Schema(description = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime releaseTime;
}
