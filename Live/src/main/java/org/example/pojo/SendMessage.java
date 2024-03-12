package org.example.pojo;

import lombok.Data;

@Data
public class SendMessage {
    private Integer type; //1为人数，2为点赞，3为弹幕，4为加入直播间的用户
    private Object content;
}
