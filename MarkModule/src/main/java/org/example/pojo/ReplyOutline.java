package org.example.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReplyOutline {
    private String replyId;
    private String markId;

    private Integer userId;
    @Schema(description = "名字")
    private String userName;
    @Schema(description = "头像")
    private String userImage;
    @Schema(description = "等级")
    private Integer level;

    @Schema(description = "内容")
    private String content;
    @Schema(description = "喜欢")
    private Integer likes;
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime time;

    private Boolean isLike;
}
