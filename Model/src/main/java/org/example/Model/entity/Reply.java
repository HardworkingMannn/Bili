package org.example.Model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reply")
public class Reply {
    @TableId
    private String replyId;
    private String markId;
    private Integer userId;

    private String parentId;
    private Integer parentType;

    private String content;
    private Integer likes;
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime time;
}
