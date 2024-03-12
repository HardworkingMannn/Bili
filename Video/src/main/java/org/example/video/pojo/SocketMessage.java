package org.example.video.pojo;

import lombok.Data;

@Data
public class SocketMessage {//发送下列属性的json格式
    private Integer type;  //1为暂停，2为恢复，3为进度跳跃，4为发送弹幕
    private Double seconds;  //进度，秒为单位
    private String content; //弹幕内容，发送弹幕时需要，其他不需要
}
