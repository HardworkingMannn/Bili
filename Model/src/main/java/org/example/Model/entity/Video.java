package org.example.Model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("video")
public class Video {
    @TableId
    private String videoId;
    private Integer userId;
    private String coverImageLink;
    private String title;
    private Integer totalHours;
    private Integer totalMinutes;
    private Integer totalSeconds;
    private String partitions;
    private String subPartition;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime releaseTime;
    private Boolean isPublished;
    private Integer plays;  //播放量
    private Integer marks;  //评论数
    private Integer danmus;
    private Integer likes;  //喜欢数量
    private Integer coins;  //硬币数
    private Integer collects;   //收藏数
    private Integer forwards; //转发量
    private Integer weight;   //计算权重，观看3，like3，mark5
}
