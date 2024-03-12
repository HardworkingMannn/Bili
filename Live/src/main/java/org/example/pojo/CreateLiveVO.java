package org.example.pojo;

import lombok.Data;

@Data
public class CreateLiveVO {
    private String httpPullUrl;
    private String hlsPullUrl;
    private String pushUrl;
    private String rtmpPullUrl;
    private String name;
    private Long ctime;
    private String cid;
}
