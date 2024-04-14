package org.example.video.utils;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

public class ScalableBloomFilter {
    @Autowired
    private RedissonClient client;
    private String prefix;
    //redis中同一个组多个布隆过滤器
    public final static long defaultSize=10;
    public final static double defaultP=0.01;
    public final static double defaultThreshold=0.75;   //默认阈值，如果超出阈值就扩容，为什么要设置阈值，因为在高并发情况下如果不加锁，过滤器添加的数据可能会超出范围导致出错概率增加，所以可以在范围的前面开始扩容
    private double threshold=defaultThreshold;
    private double p=defaultP;

    public ScalableBloomFilter(String prefix){//布隆过滤器组的前缀
        this(prefix,defaultSize,defaultP);
    }
    public ScalableBloomFilter(String prefix,long initSize,double initP){
        this.prefix=prefix;
        this.p=initP;
        Jedis jedis = JedisUtil.getJedis();
        try {
            //检测组是否存在，如果不存在，重新创建
            String bfKey1 = "bf_" + prefix + 1;
            if (!jedis.exists(bfKey1)) {//还得加锁
                RLock lock = client.getLock(bfKey1 + "_lock");
                lock.lock();
                try {
                    if (!jedis.exists(bfKey1)) {
                        RBloomFilter<Object> filter = client.getBloomFilter(bfKey1);
                        filter.tryInit(initSize, initP);
                        //组序号
                        String bfSize = "bf_" + prefix + "_size"; //既是大小也是序号
                        String bfMax = "bf_" + prefix + "_max";   //当前过滤器的最大值
                        jedis.set(bfSize, "1");
                        jedis.set(bfMax, initSize + "");
                    }
                } finally {
                    lock.unlock();
                }
            }
        }finally {
            jedis.close();
        }
    }

    public void addElement(String s){
        String bfSize="bf_"+prefix+"_size"; //既是大小也是序号
        String bfMax="bf_"+prefix+"_max";   //当前过滤器的最大值
        Jedis jedis = JedisUtil.getJedis();
        try {
            int index = Integer.parseInt(jedis.get(bfSize));
            int max = Integer.parseInt(jedis.get(bfMax));
            String bfKey = "bf_" + prefix + index;
            RBloomFilter filter = client.getBloomFilter(bfKey);
            if(filter.sizeInMemory()<(long)(max*threshold)){
                filter.add(s);
            }else{
                //扩容
                String growKey="bf_" + prefix+"grow_lock";
                RLock lock = client.getLock(growKey);
                lock.lock();
                try{
                    String bfNewKey = "bf_" + prefix + index+1; //有个隐患，在扩容期间，有大量添加操作阻塞，导致添加操作可能会大于扩容的下一个过滤器最大的大小
                    if(jedis.exists(bfNewKey)){
                        RBloomFilter newFilter = client.getBloomFilter(bfNewKey);
                        newFilter.add(s);
                    }else{
                        RBloomFilter newFilter = client.getBloomFilter(bfNewKey);
                        newFilter.tryInit(max*10,p);
                        newFilter.add(s);
                        jedis.incr(bfSize);
                        jedis.set(bfMax,max*10+"");
                    }
                }finally {
                    lock.unlock();
                }
            }

        }finally {
            jedis.close();
        }
    }
    public boolean contain(String s){
        String bfSize="bf_"+prefix+"_size"; //既是大小也是序号
        Jedis jedis = JedisUtil.getJedis();
        try {
            int index = Integer.parseInt(jedis.get(bfSize));
            for (int i = 1; i <= index; i++) {
                String bfKey = "bf_" + prefix + i;
                RBloomFilter filter = client.getBloomFilter(bfKey);
                if(filter.contains(s)){
                    return true;
                }
            }
        }finally {
            jedis.close();
        }
        return false;
    }
}
