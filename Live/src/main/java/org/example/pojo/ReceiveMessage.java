package org.example.pojo;

import lombok.Data;

@Data
public class ReceiveMessage {
    private Integer type;  //1为弹幕,2为喜欢
    private String content;
}
