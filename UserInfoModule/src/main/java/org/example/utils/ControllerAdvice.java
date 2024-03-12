package org.example.utils;

import org.example.video.Model.pojo.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/*@RestControllerAdvice*/
public class ControllerAdvice {
    /*@ExceptionHandler*/
    public Result handler(Exception e){
        return Result.fail(e.getMessage());
    }
}
