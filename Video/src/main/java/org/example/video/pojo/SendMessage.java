package org.example.video.pojo;

import lombok.Data;

@Data
public class SendMessage {
    private Integer type;   //1为弹幕，2为人数
    private String message; //如果是弹幕就是弹幕内容，如果是人数就是人数
}
