package org.example.Model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class GetMarksDTO {
    @Schema(description = "如果是动态就是动态id，如果是视频就是视频id")
    private String parentId;
    /*@Schema(description = "如果是动态就是1，如果是视频就是2")
    private Integer parentType;*/
    @Schema(description = "页数，从0开始")
    private Integer pageNum;
    @Schema(description = "页大小")
    private Integer pageSize;
    @Schema(description = "是否按热度排序")
    private Boolean heat;
}
