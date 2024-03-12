package org.example.Model.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class CallNotifyVO {
    private Integer userId;
    private String userImage;
    private String userName;

    private String parentId;
    private Integer fromType;
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime time;
}
