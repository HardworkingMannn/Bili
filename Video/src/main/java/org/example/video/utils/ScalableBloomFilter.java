package org.example.video.utils;

import org.redisson.api.RBloomFilter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

public class ScalableBloomFilter {
    private List<RBloomFilter> list;
    private long nowMax; //目前最新的布隆过滤器的最大大小
    private RBloomFilter now;
    public ScalableBloomFilter(){
        list=new ArrayList<>();
        nowMax=0;
    }
    public void add(RBloomFilter filter){//初始添加
        list.add(filter);
        nowMax=filter.getExpectedInsertions();
        now=filter;
    }
    public void addElement(Object o){

    }
}
