package org.example.Model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ReplyDTO implements Comment{
    @Schema(description = "视频作者的id或者动态作者的id")
    private Integer authorId;
    @Schema(description = "来源的ID，可能是视频ID，也可能是动态ID")
    private String parentId;
    @Schema(description = "来源类型，是视频(2)还是动态(1)")
    private Integer fromType;
    @Schema(description = "评论的人的ID")
    private String markUserId;
    @Schema(description = "被回复的人的ID，可为空，因为可能不是在里面回复的")
    private String replyId;

    @Schema(description = "评论的ID")
    private String markId;
    @Schema(description = "内容")
    private String content;
}
