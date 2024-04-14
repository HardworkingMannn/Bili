package org.example.video.config;

import org.example.video.Model.constant.JedisConst;
import org.example.video.utils.JedisUtil;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://"+ JedisConst.JEDIS_HOST+":"+JedisConst.JEDIS_PORT);
        return Redisson.create(config);
    }
}
