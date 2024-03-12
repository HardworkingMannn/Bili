package org.example.Model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("user_info")
public class UserInfo {
    @TableId
    private Integer userId;
    private String userImage;
    private String userName;

    private String signature;
    @Schema(description = "性别类型，1为男，2为女，3为保密")
    private String sexType;
    @Schema(description = "生日")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
    @Schema(description = "大学")
    private String school;
    @Schema(description = "等级")
    private Integer level;
    @Schema(description = "经验")
    private Integer exp;
    @Schema(description = "关注数")
    private Integer follows;
    @Schema(description = "粉丝数")
    private Integer fans;
    @Schema(description = "大学")
    private Integer likes;
    @Schema(description = "播放量")
    private Integer plays;
    @Schema(description = "硬币数")
    private Integer coins;
    @Schema(description = "收藏数")
    private Integer collects;
    @Schema(description = "稿件数量")
    private Integer published;
    @Schema(description = "动态数量")
    private Integer dongtais;
    @Schema(description = "公告")
    private String notice;
    private Integer marks;
    private Integer danmus;
    private Integer forwards;
}
