package com.example.chatroom.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "注册接口传输对象")
public class RegisterDTO {
    @Schema(description = "邮箱")
    private String username;
    private String password;
    @Schema(description = "验证码")
    private String code;
}
