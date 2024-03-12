package org.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.Model.entity.Reply;
import org.example.pojo.ReplyOutline;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MarkRepresent {
    private String markId;
    private Integer userId;
    private String userImage;
    private String userName;
    @Schema(description = "等级")
    private Integer level;

    @Schema(description = "评论内容")
    private String content;
    @Schema(description = "评论图片")
    private List<String> images;

    @Schema(description = "回复")
    private List<ReplyOutline> replys;
    @Schema(description = "回复数")
    private Integer marks;
    @Schema(description = "喜欢数")
    private Integer likes;
    @Schema(description = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime time;

    private Boolean isLike;
}
