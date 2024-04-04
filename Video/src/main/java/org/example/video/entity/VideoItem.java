package org.example.video.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("video_item")
public class VideoItem {
    private String videoId;
    @TableId
    private String videoItemId;
    private String videoLink;
    private String videoTitle;

    private Integer maxTimeStamp;
    private Integer hours;
    private Integer minutes;
    private Integer seconds;
}
