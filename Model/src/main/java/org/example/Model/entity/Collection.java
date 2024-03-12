package org.example.Model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import jdk.jfr.DataAmount;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("collection")
public class Collection {
    @TableId
    private String collectionId;
    private Integer userId;
    private String collectionName;
    private Integer count;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime time;
}
