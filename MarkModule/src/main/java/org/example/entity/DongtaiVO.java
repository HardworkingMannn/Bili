package org.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.Model.pojo.DongtaiOutline;
import org.example.Model.pojo.VideoOutline;
import org.w3c.dom.stylesheets.LinkStyle;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DongtaiVO {
    @Schema(description = "动态ID")
    private String markId;
    private Integer userId;
    private String userName;
    private String userImage;
    @Schema(description = "内容")
    private String content;
    @Schema(description = "如果是动态类型（1）dongtaiOutline不为空，如果是视频类型（2）videoOutline不为空，如果是0，则代表没有转发")
    private Integer quoteType;
    private VideoOutline videoOutline;
    private DongtaiOutline dongtaiOutline;

    private Integer forwards;
    @Schema(description = "评论数")
    private Integer marks;
    @Schema(description = "喜欢数")
    private Integer likes;
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    @Schema(description = "发布时间")
    private LocalDateTime time;
    @Schema(description = "图片")
    private List<String> images;

    private Boolean isLike;
}
