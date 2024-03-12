package org.example.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class HomePageVO {
    private String userName;
    private String userImage;

    private Integer level;
    private Integer exp;
    @Schema(description = "下个等级所需经验值")
    private Integer nextExp;
    private Integer coins;
    private Integer follows;
    private Integer fans;
    private Integer dongtais;
}
