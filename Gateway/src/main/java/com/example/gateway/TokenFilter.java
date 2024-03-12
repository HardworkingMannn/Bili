package com.example.gateway;

import lombok.extern.slf4j.Slf4j;
import org.example.Model.entity.DailyMission;
import org.example.video.Model.constant.JwtConst;
import org.example.video.Model.pojo.Result;
import org.example.video.utils.JedisUtil;
import org.example.video.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import redis.clients.jedis.Jedis;

import java.util.List;

@Component
@Slf4j
public class TokenFilter implements GlobalFilter {
    @Autowired
    private WhiteList whiteList;
    private AntPathMatcher matcher=new AntPathMatcher();/*
    @Autowired
    private UserInfoClient infoClient;*/

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        if (checkWhiteList(request.getPath().value())) {//把登录接口排除
            return chain.filter(exchange);
        }

        List<String> list = request.getHeaders().get("token");
        if(list==null){
            log.warn("未携带token,不通过");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        String token = list.get(0);
        Jedis jedis=null;
        try {
            jedis= JedisUtil.getJedis();
            String id = jedis.get(token);
            Integer userId;
            if (id == null) {
                userId = JwtUtil.verifyToken(token);
                if (userId != null) {
                    jedis.setex(token, JwtConst.JWT_EXIST_HOUR * 60 * 60, "" + userId);   //设置在redis中，让其他共享

                    //如果不经过登录接口的其他接口通过，就代表完成了每日任务登录
                /*DailyMission data = infoClient.getDailyMission(userId).getData();
                if(!data.getDailyReg()) {
                    data.setDailyReg(true);
                    infoClient.editDailyMission(data);
                }*/

                    return chain.filter(exchange);
                }
                log.info("token{}不通过检验", token);
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            } else {
                userId = Integer.parseInt(id);
                //如果不经过登录接口的其他接口通过，就代表完成了每日任务登录
            /*DailyMission data = infoClient.getDailyMission(userId).getData();
            if(!data.getDailyReg()) {
                data.setDailyReg(true);
                infoClient.editDailyMission(data);
            }*/
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }
        return chain.filter(exchange);
    }
    public boolean checkWhiteList(String path){
        log.info(path);
        String[] pass = whiteList.getPass();
        for (String s : pass) {
            if(matcher.match(s,path)){
                log.info(path+"是白名单路径，通过");
                return true;
            }
        }
        return false;
    }
}
