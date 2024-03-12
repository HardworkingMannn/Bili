package org.example.ws;

import com.alibaba.fastjson.JSON;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.example.Model.entity.UserInfo;
import org.example.mapper.UserInfoMapper;
import org.example.pojo.DanmuVO;
import org.example.pojo.ReceiveMessage;
import org.example.pojo.SendMessage;
import org.example.utils.SpringApplicationContextHolder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/live/{cid}/{uid}")
@Component
@Slf4j
public class LiveSocket {
    public static final ConcurrentHashMap<String,Integer> playMap=new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String,Integer> likeMap=new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, List<Session>> sessionMap=new ConcurrentHashMap<>();
    @Autowired
    private UserInfoMapper userInfoMapper;
    public LiveSocket(){
        log.info("执行构造方法");
        userInfoMapper = SpringApplicationContextHolder.getBean(UserInfoMapper.class);
    }
    @OnOpen
    public void open(Session session, @PathParam("cid")String cid,@PathParam("uid")Integer uid){
        log.info("连接到ws");
        if(!playMap.containsKey(cid)){
            playMap.put(cid,0);
            likeMap.put(cid,0);
            sessionMap.put(cid,new ArrayList<>());
        }
        Integer i = playMap.get(cid);
        i++;
        playMap.put(cid,i);
        List<Session> sessions = sessionMap.get(cid);
        sessions.add(session);
        //发消息
        sendPlay(cid);
        sendJoin(cid,uid);
    }
    @OnClose
    public void close(Session session,@PathParam("cid")String cid,@PathParam("uid")Integer uid){
        log.info("连接断开:" + cid+" "+uid);
        sessionMap.get(cid).remove(session);
        //不需要通知
    }
    @OnMessage
    public void message(String message,@PathParam("cid")String cid,@PathParam("uid")Integer uid){//发送弹幕或者点赞
        ReceiveMessage receiveMessage = JSON.parseObject(message, ReceiveMessage.class);
        if(receiveMessage.getType()==1){
            log.info("滇藏");
            likeMap.put(cid,likeMap.get(cid)+1);
            sendLike(cid);
        }else if(receiveMessage.getType()==2){
            log.info("前端发送弹幕,{}",receiveMessage.getContent());
            sendDanmu(cid,receiveMessage.getContent(),uid);
        }
    }
    public void sendDanmu(String cid,String danmu,Integer uid){
        UserInfo info = userInfoMapper.selectById(uid);
        DanmuVO vo=new DanmuVO();
        BeanUtils.copyProperties(info,vo);
        vo.setContent(danmu);
        SendMessage message = new SendMessage();
        message.setType(3);
        message.setContent(vo);
        String jsonString = JSON.toJSONString(message);
        List<Session> sessions = sessionMap.get(cid);
        for (Session session : sessions) {
            session.getAsyncRemote().sendText(jsonString);
        }
    }
    public void sendLike(String cid){
        SendMessage message = new SendMessage();
        message.setType(2);
        message.setContent(likeMap.get(cid));
        String jsonString = JSON.toJSONString(message);
        List<Session> sessions = sessionMap.get(cid);
        for (Session session : sessions) {
            session.getAsyncRemote().sendText(jsonString);
        }
    }
    public void sendPlay(String cid){
        SendMessage message = new SendMessage();
        message.setType(1);
        message.setContent(playMap.get(cid));
        String jsonString = JSON.toJSONString(message);
        List<Session> sessions = sessionMap.get(cid);
        for (Session session : sessions) {
            session.getAsyncRemote().sendText(jsonString);
        }
    }
    public void sendJoin(String cid,Integer uid){
        SendMessage message = new SendMessage();
        message.setType(4);
        message.setContent(userInfoMapper.selectById(uid).getUserName());
        String jsonString = JSON.toJSONString(message);
        List<Session> sessions = sessionMap.get(cid);
        for (Session session : sessions) {
            session.getAsyncRemote().sendText(jsonString);
        }
    }
    public int getPlay(String cid){
        return playMap.get(cid);
    }
}
