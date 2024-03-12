package org.example.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InitInfo {
    @NotNull(message = "用户名字不能为空")
    @Schema(description = "用户名")
    private String userName;
    @NotNull(message = "用户头像不能为空")
    @Schema(description = "用户头像")
    private String userImage;
}
