package org.example.Model.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReplyNotifyVO {
    private UserSimpleInfo info;
    @Schema(description = "回复的内容")
    private String replyContent;
    @Schema(description = "被回复的内容")
    private String content;
    @Schema(description = "回复来源，视频或者动态")
    private Integer fromType;
    @Schema(description = "来源ID，可能是videoId或dongtaiId")
    private String parentId;

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime time;
}
