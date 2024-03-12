package org.example.Model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("mark")
public class Mark {
    @TableId
    private String markId;
    private Integer userId;

    private Integer markType;   //是动态还是普通回复

    private String parentId;      //回复的ID，可能是动态，也可能是视频
    private Integer parentType; //回复的类型

    private String quoteId;
    private Integer quoteType;


    private String content;
    private Integer forwards; //转发数
    private Integer marks;
    private Integer likes;
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime time;
    private Integer weight;
}
