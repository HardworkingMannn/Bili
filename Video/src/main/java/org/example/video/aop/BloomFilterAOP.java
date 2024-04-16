package org.example.video.aop;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.example.video.Model.pojo.Result;
import org.example.video.pojo.SubmitDTO;
import org.example.video.pojo.VideoInfo;
import org.example.video.utils.ScalableBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class BloomFilterAOP {
    @Resource
    private ScalableBloomFilter videoScalableBloomFilter;
    @Pointcut("execution(* org.example.video.controller.UploadVideoController.*(*))&&@annotation(org.example.Model.annotation.GetFilter)")
    public void filter(){
    }
    @Pointcut("execution(* org.example.video.controller.UploadVideoController.*(*))&&@annotation(org.example.Model.annotation.AddFilter)")
    public void addFilter(){
    }
    @Around("filter()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        for (Object arg : joinPoint.getArgs()) {
            if(arg instanceof String){
                if(!videoScalableBloomFilter.contain((String)arg)){
                    log.info("拦截请求，videoId为{}",arg);
                    return Result.fail("没有该内容");
                }
                break;
            }
        }
        Object proceed = joinPoint.proceed();
        return proceed;
    }
    @AfterReturning(value="addFilter()",returning = "result")
    public void afterReturn(JoinPoint joinPoint,Object result){
        if(result instanceof Result<?>){
            Result res=(Result)result;
            if(res.getCode()==200){//成功就会添加
                for (Object arg : joinPoint.getArgs()) {
                    if(arg instanceof SubmitDTO){
                        SubmitDTO dto=(SubmitDTO) arg;
                        for (VideoInfo video : dto.getVideos()) {
                            videoScalableBloomFilter.addElement(video.getVideoItemId());
                        }
                        break;
                    }
                }
            }
        }
    }
}
