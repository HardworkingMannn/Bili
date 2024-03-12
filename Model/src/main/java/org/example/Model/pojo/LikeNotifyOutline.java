package org.example.Model.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class LikeNotifyOutline {
    @Schema(description = "包含用户头像，名字，id，最多两个")
    private List<UserSimpleInfo> infos;
    private Integer likes;
    @Schema(description = "类型，是评论还是视频,1为视频，2为评论")
    private Integer likeType;
    private Long videoId;
    @Schema(description = "可能是视频封面链接，也可能是回复信息")
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime time;
}
