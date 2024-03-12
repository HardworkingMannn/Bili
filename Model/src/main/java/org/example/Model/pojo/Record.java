package org.example.Model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Record<T> {
    private T record;
    @Schema(description = "总数")
    private Long total;
    @Schema(description = "页数，从0开始")
    private Integer pageNum;
    @Schema(description = "页大小")
    private Integer pageSize;
}
