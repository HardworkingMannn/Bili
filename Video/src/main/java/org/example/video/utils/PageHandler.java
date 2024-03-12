package org.example.video.utils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Component
@Aspect
public class PageHandler {
    @Pointcut("execution(* org.example.video.controller.UploadVideoController.*(*))&@annotation(org.example.Model.annotation.Page)")
    public void pointCut(){

    }

}
