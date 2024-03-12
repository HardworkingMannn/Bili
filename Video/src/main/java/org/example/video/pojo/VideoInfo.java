package org.example.video.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoInfo {
    private String videoItemId;
    private String videoLink;
    private String videoTitle;
    private Integer hours;
    private Integer minutes;
    private Integer seconds;
}
