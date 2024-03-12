package com.example.chatroom.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "通用返回结果")
public class Result<T> {
    @Schema(description = "状态码，代表是否成功,0代表未成功，1代表成功")
    private int code;  //状态码，代表是否成功,0代表未成功，1代表成功
    @Schema(description = "返回信息")
    private String message;    //返回信息
    @Schema(description = "返回对象")
    private T data;           //结果对象
    public static <T> Result  success(T o){
        return new Result(200,"success",o);
    }
    public static Result fail(String message){  //失败时不返回对象，返回错误的状态码
        return new Result(0,message,null);
    }
    public static Result success(){
        return new Result(200,"success",null);
    }
}
