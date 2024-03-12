package org.example.Model.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoManagement {
    private String videoId;
    private String coverImageLink;
    private String title;
    private Integer totalHours;
    private Integer totalMinutes;
    private Integer totalSeconds;
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime releaseTime;
    private Integer plays;  //播放量
    private Integer marks;  //评论数
    private Integer danmus;
    private Integer likes;  //喜欢数量
    private Integer coins;  //硬币数
    private Integer collects;   //收藏数
    private Integer forwards; //转发量
}
