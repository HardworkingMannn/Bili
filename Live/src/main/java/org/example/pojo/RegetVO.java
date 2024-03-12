package org.example.pojo;

import lombok.Data;

@Data
public class RegetVO {
    private String httpPullUrl;
    private String hlsPullUrl;
    private String pushUrl;
    private String rtmpPullUrl;
}
