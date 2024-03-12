package org.example.aop;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.example.Model.entity.CallNotify;
import org.example.Model.entity.UserInfo;
import org.example.Model.pojo.Comment;
import org.example.Model.pojo.MarkDTO;
import org.example.mapper.CallNotifyMapper;
import org.example.mapper.UserInfoMapper;
import org.example.utils.AsyncHandler;
import org.example.video.utils.ThreadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class CallAdvice {
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private AsyncHandler asyncHandler;
    @Pointcut("execution(* org.example.service.serviceImpl.MarkServiceImpl.*(*))&&@annotation(org.example.Model.pojo.CallHandler)")
    public void pointCut(){}
    @Before("pointCut()")
    public void before(JoinPoint joinPoint){//处理@
        log.info("调用aop方法处理@");
        for (Object arg : joinPoint.getArgs()) {
            if(arg instanceof Comment){
                Comment comment=(Comment) arg;
                String content = comment.getContent();
                log.info("发送的消息为{}",content);
                handle(content,comment);
            }
        }
    }
    public void handle(String content,Comment comment){
        int length = content.length();
        StringBuilder sb=new StringBuilder();
        int last=0;
        for (int i = 0; i < length; i++) {
            if(content.charAt(i)=='@'){
                sb.append(content.substring(last,i));
                int end=i+1;
                while(content.charAt(end)!=' '){
                    end++;
                }
                UserInfo info = userInfoMapper.selectOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUserName, content.substring(i + 1, end)));
                if(info!=null){
                    asyncHandler.addCallNotify(comment,info.getUserId());
                    sb.append("<span class=\"call id_"+info.getUserId()+"\">");
                    sb.append(content.substring(i, end));
                    sb.append("</span>");
                }else{
                    String substring = content.substring(i, end);
                    log.info("用户名{}不存在",substring);
                    sb.append(substring);
                }
                last=end;
                i=end+1;
            }
        }
        sb.append(content.substring(last,length));
        comment.setContent(sb.toString());
        log.info("@处理完成，最终结果为",comment.getContent());
    }

}
