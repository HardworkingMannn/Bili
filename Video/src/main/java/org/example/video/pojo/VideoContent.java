package org.example.video.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class VideoContent {
    //TODO 缺个用户信息
    @Schema(description = "视频发布者的userId")
    private Integer userId;
    private String userName;
    private String userImage;
    @Schema(description = "视频发布者的个人签名")
    private String signature;
    private Integer fans;
    @Schema(description = "是否关注")
    private Boolean isFollowed;

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    @Schema(description = "发布时间")
    private LocalDateTime releaseTime;
    @Schema(description = "播放量")
    private Integer plays;  //播放量
    @Schema(description = "评论数")
    private Integer marks;  //评论数
    private Integer danmus;
    private String title;
    @Schema(description = "视频数组，内部是视频链接，分p视频标题，视频长度")
    private List<VideoInfo> videoInfos;

    private Integer likes;  //喜欢数量
    private Integer coins;  //硬币数
    private Integer collects;   //收藏数
    @Schema(description = "转发数")
    private Integer forwards; //转发量

    private Boolean isLike;
    private Boolean isCoins;


    @Schema(description = "标签")
    private List<String> tags;
    @Schema(description = "简介")
    private String description;

}
