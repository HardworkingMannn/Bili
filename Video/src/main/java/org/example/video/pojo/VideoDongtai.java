package org.example.video.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoDongtai {
    private Integer userId;
    private String userName;
    private String userImage;

    private Integer videoId;
    private String coverImageLink;
    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime releaseTime;
}
