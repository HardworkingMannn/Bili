package org.example.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.video.Model.constant.JwtConst;
import org.example.video.utils.JedisUtil;
import org.example.video.utils.JwtUtil;
import org.example.video.utils.ThreadUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import redis.clients.jedis.Jedis;

@Component
@Slf4j
public class TokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        if(token==null){//因为网关已经检测过了，懒得检测了
            return false;
        }
        Integer u=null;
        Jedis jedis=null;
        try {
            jedis = JedisUtil.getJedis();
            String idStr = jedis.get(token);
            if (idStr == null) {
                u = JwtUtil.verifyToken(token);
                if (u == null) {
                    log.info("token无效");
                    return false;
                }
                jedis.setex(token, JwtConst.JWT_EXIST_HOUR * 60 * 60, "" + u);
        }else{
            u=Integer.parseInt(idStr);
        }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if (jedis != null) {
                jedis.close();
            }
        }
        log.info("从token中获取userid:{}",u);
        ThreadUtils.set(u);
        return true;
    }
}
