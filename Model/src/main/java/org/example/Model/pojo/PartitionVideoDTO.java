package org.example.Model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PartitionVideoDTO {
    @Schema(description = "分区")
    private String partitions;
    @Schema(description = "子分区")
    private String subPartition;
    @Schema(description = "页号")
    private Integer pageNum;
    @Schema(description = "页大小")
    private Integer pageSize;
}
