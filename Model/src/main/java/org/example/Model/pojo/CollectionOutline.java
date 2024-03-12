package org.example.Model.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CollectionOutline {
    private String collectionId;
    @Schema(description = "合集名字")
    private String collectionName;
    private Integer count;
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime time;
    @Schema(description = "封面")
    private String coverImageLink;
}
