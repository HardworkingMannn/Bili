package org.example.Model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class MarkDTO implements Comment{
    @Schema(description = "如果是动态就是动态id，如果是视频就是视频id")
    private String parentId;
    @Schema(description = "如果是动态就是动态作者的id，如果是视频就是视频作者的id，用于通知")
    private Integer authorId;
    @Schema(description = "来源类型，是视频(2)还是动态(1)")
    private Integer fromType;
    @Schema(description = "内容")
    private String content;
    @Schema(description = "图片")
    private List<String> images;
    private String markId;
    private String replyId;
}
