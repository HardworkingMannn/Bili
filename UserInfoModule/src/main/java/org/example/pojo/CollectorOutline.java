package org.example.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data

public class CollectorOutline {
    private Integer collectorId;
    @Schema(description = "收藏夹名字")
    private String collectorName;
    @Schema(description = "收藏夹内视频数量")
    private Integer count;
    @Schema(description = "收藏夹是否公开")
    private Boolean isPrivate;
    @Schema(description = "收藏夹封面")
    private String coverImageLink;
}
