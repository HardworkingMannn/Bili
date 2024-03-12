package org.example.video.ws;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.example.Model.entity.Danmu;
import org.example.video.constant.MessageType;
import org.example.video.constant.SendMessageType;
import org.example.video.entity.PausableScheduledThreadPoolExecutor;
import org.example.video.mapper.DanmuMapper;
import org.example.video.pojo.SendMessage;
import org.example.video.pojo.SocketMessage;
import org.example.video.service.serviceImpl.DanmuService;
import org.example.video.utils.SpringApplicationContextHolder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.Timer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@ServerEndpoint("/video/danmu/{uid}/{vid}") //uid是用户id，vid是视频item的id
@Slf4j
@Component
public class DanmuSocket {
    private static ConcurrentHashMap<String, Session> sessionMap=new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String,Integer> indexMap=new ConcurrentHashMap<>();  //这个待会单独搞一个
    private static ConcurrentHashMap<String, PausableScheduledThreadPoolExecutor> serviceMap=new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Long> timerMap=new ConcurrentHashMap<>();
    private static volatile AtomicInteger num=new AtomicInteger(0);
    @Autowired
    private DanmuMapper danmuMapper;
    public DanmuSocket(){
        log.info("执行构造方法");
        danmuMapper = SpringApplicationContextHolder.getBean(DanmuMapper.class);
    }
    @OnOpen
    public void open(Session session,@PathParam("uid")String uid,@PathParam("vid")String vid){
        String str=uid+"_"+vid;
        sessionMap.put(str,session);
        log.info("{}连接成功",uid+"_"+vid);
        //初始化一堆map
        indexMap.put(str,0);
        timerMap.put(str,System.currentTimeMillis());
        //增加人数
        num.incrementAndGet();
        //通知
        sendNum(vid);
        newPool(uid,vid,str,true);

    }
    private void newPool(String uid, String vid, String str,boolean start) {
        PausableScheduledThreadPoolExecutor pool = new PausableScheduledThreadPoolExecutor(10);
        serviceMap.put(str, pool);
        if(start){
            pool.pause();
        }else {
            run(uid, vid);
        }
    }
    @OnMessage
    public void onMessage(String message,@PathParam("uid")String uid,@PathParam("vid")String vid){
        log.info("收到来自客户端：" + uid+"_"+vid + "的信息:" + message);
        String str=combine(uid,vid);
        //message要有一定格式
        SocketMessage parsed = JSON.parseObject(message, SocketMessage.class);
        PausableScheduledThreadPoolExecutor pool = serviceMap.get(str);
        if(parsed.getType()==MessageType.PAUSE){
            log.info("用户{}的{}视频暂停",uid,vid);
            pool.pause();
        }else if(parsed.getType()==MessageType.UNPAUSE){
            log.info("用户{}的{}视频解除暂停",uid,vid);
            long pause = pool.unpause();
            log.info("暂停时间{}",pause);
            timerMap.put(str,timerMap.get(str)+pause);
            log.info("删除线程池");
            PausableScheduledThreadPoolExecutor executor = serviceMap.get(str);
            executor.shutdown();
            serviceMap.remove(str);
            log.info("重新创建线程池");
            newPool(uid,vid,str,false);
        }else if(parsed.getType()==MessageType.JUMP){
            log.info("消息类型为进度跳跃");
            Long startTime = timerMap.get(str);
            long now=System.currentTimeMillis()- startTime;
            long jumpTo = calculateTime(parsed);
            long def=now-jumpTo;
            log.info("相差{}ms",def);
            timerMap.put(str, startTime +def);
            log.info("删除线程池");
            PausableScheduledThreadPoolExecutor executor = serviceMap.get(str);
            executor.shutdown();
            serviceMap.remove(str);
            log.info("重新创建线程池");
            newPool(uid,vid,str,false);
        }else if(parsed.getType()==MessageType.SEND){
            long def=calculateTime(parsed);
            if(def>=0){
                Danmu danmu = new Danmu();
                danmu.setContent(parsed.getContent());
                danmu.setTime(def);
                danmu.setVideoItemId(vid);
                danmuMapper.insert(danmu);
            }
        }
    }
    public String combine(String uid,String vid){
        return uid+"_"+vid;
    }
    @OnClose
    public void onClose(@PathParam("uid")String uid,@PathParam("vid")String vid) {
        log.info("连接断开:" + uid+"_"+vid);
        sessionMap.remove(uid+"_"+vid);
        timerMap.remove(uid+"_"+vid);
        serviceMap.remove(uid+"_"+vid);
        num.decrementAndGet();
        sendNum(vid);
    }
    public static void sendDanmu(String uid,String vid,String content){
        SendMessage message = new SendMessage();
        message.setType(SendMessageType.DANMU);
        message.setMessage(content);
        String jsonString = JSON.toJSONString(message);
        sendMessage(uid, vid, jsonString);
    }
    public void sendNum(String vid){
        log.info("发送实时在线人数给所有人");
        SendMessage message = new SendMessage();
        message.setType(SendMessageType.NUM);
        message.setMessage(num.get()+"");
        String jsonString = JSON.toJSONString(message);
        for (String s : sessionMap.keySet()) {
            if(s.endsWith(vid)){
                Session session = sessionMap.get(s);
                session.getAsyncRemote().sendText(jsonString);
            }
        }
    }
    public static void sendMessage(String uid,String vid,String message){
        String key=uid+"_"+vid;
        Session session = sessionMap.get(key);
        session.getAsyncRemote().sendText(message);
    }
    public void run(String uid,String vid) {
        String combine = combine(uid, vid);
        log.info("开始执行"+combine+"的定时弹幕任务");
        if(timerMap.get(combine)==null){
            timerMap.put(combine,System.currentTimeMillis());
        }
        long startTime=timerMap.get(combine);
        log.info("当前videoId{}",vid);
        log.info("当前播放进程{}ms",System.currentTimeMillis()-startTime);
        List<Danmu> list = danmuMapper.selectList(Wrappers.<Danmu>lambdaQuery().eq(Danmu::getVideoItemId,vid).orderByAsc(Danmu::getTime));
        list.forEach(danmu -> {
            submitTask(danmu, startTime,uid,vid);
        });
        log.info("发给{}的{}视频的弹幕已经发送完成",uid,vid);
    }
    public boolean checkTime(Danmu danmu,long startTime){
        long remaining=danmu.getTime()-(System.currentTimeMillis()-startTime);
        if(remaining<=10*1000){
            return false;
        }
        return true;
    }
    public void submitTask(Danmu danmu,long startTime,String uid,String vid){
        String combine=combine(uid,vid);
        PausableScheduledThreadPoolExecutor service = serviceMap.get(combine);
        long remaining=danmu.getTime()-(System.currentTimeMillis()-startTime);
        if(remaining>=-500) {
            log.info("{}ms误差在500ms之内",remaining);
            service.schedule(new SendDanmu(uid, vid, danmu.getContent()), remaining, TimeUnit.MILLISECONDS);
        }
    }
    public long calculateTime(SocketMessage message){
        return (long)(message.getSeconds()*1000);
    }
}
@Slf4j
class SendDanmu implements Runnable{
    private String uid;
    private String vid;
    private String content;
    public SendDanmu(String uid, String vid,String content) {
        this.uid = uid;
        this.vid = vid;
        this.content=content;
    }

    @Override
    public void run() {
        log.info("发送弹幕");
        DanmuSocket.sendDanmu(uid,vid,content);
    }
}
