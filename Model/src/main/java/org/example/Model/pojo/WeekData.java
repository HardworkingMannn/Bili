package org.example.Model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import lombok.Data;

@Data
public class WeekData {
    @Schema(description = "播放量")
    private Integer plays;
    @Schema(description = "粉丝数")
    private Integer fans;
    private Integer likes;
    @Schema(description = "收藏数")
    private Integer collects;
    @Schema(description = "硬币数")
    private Integer coins;
    private Integer marks;
    private Integer danmus;
    private Integer forwards;


}
