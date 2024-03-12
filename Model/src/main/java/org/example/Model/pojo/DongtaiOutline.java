package org.example.Model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class DongtaiOutline {
    private Integer userId;
    private String userImage;
    private String userName;
    @Schema(description = "动态ID")
    private String markId;
    private String content;
    @Schema(description = "图片")
    private List<String> images;

}
