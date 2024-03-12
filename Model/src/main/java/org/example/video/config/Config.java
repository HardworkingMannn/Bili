package org.example.video.config;

import org.example.video.Model.constant.JedisConst;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

@Configuration
public class Config {
    @Bean
    public Jedis jedis(){
        return new Jedis(JedisConst.JEDIS_HOST,JedisConst.JEDIS_PORT);
    }
}
