package org.example.Model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("like_notify")
public class LikeNotify {
    private Integer userId;
    private String parentId;
    private Integer parentType;
    private Integer markType;
    private String markId;
    private String replyId;
    private Integer likeType;

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime time;
}
