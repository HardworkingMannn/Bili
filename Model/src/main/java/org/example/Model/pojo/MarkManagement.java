package org.example.Model.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MarkManagement {
    private Integer userId;
    private String userName;
    private String userImage;
    private String markId;
    private String replyContent;
    private Integer likes;
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime time;
    private String videoId;
    private String coverImageLink;
    private String title;
}
