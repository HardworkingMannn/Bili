package org.example.video.utils;

import org.example.video.Model.constant.JedisConst;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisUtil {
    public static volatile JedisPool jedisPool=null;
    static {
        jedisPool = getJedisPoolInstance();
    }

    /**
     * 获取RedisPool实例（单例）
     * @return RedisPool实例
     */
    private static JedisPool getJedisPoolInstance() {
        if (jedisPool == null) {
            synchronized (JedisUtil.class) {
                if (jedisPool == null) {

                    JedisPoolConfig poolConfig = new JedisPoolConfig();
                    poolConfig.setMaxTotal(1000);           // 最大连接数
                    poolConfig.setMaxIdle(32);              // 最大空闲连接数
                    poolConfig.setMaxWaitMillis(100*1000);  // 最大等待时间
                    poolConfig.setTestOnBorrow(true);       // 检查连接可用性, 确保获取的redis实例可用

                    jedisPool = new JedisPool(poolConfig, JedisConst.JEDIS_HOST, JedisConst.JEDIS_PORT);
                }
            }
        }

        return jedisPool;
    }
    public static Jedis  getJedis(){
        return jedisPool.getResource();
    }
}
