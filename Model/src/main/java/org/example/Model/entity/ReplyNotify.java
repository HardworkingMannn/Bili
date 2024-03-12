package org.example.Model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reply_notify")
public class ReplyNotify {
    private Integer userId;
    private Integer fromType;
    private String parentId;
    private String markId;
    private String content;
    private String replyContent;
    private Integer replyerId;
    private Integer likes;
    private Integer marks;
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime time;
}
