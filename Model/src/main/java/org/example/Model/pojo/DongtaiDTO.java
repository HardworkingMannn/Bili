package org.example.Model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.Model.pojo.Comment;

import java.util.List;

@Data
public class DongtaiDTO implements Comment {
    @Schema(description = "内容")
    private String content;
    @Schema(description = "图片")
    private List<String> images;
    @Schema(description = "可能是转发其他动态，可能是视频下发布的动态，这是视频或动态的ID")
    private String quoteId;
    @Schema(description = "可能是转发其他动态，类型为1，可能是视频下发布的动态，类型为2，没有转发或视频就为0")
    private Integer quoteType;
    @Schema(description = "以下两个参数都是不用传的，这是我后端搞的额外的东西")
    private String parentId;
    private Integer fromType;
}
