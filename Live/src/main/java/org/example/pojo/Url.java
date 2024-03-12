package org.example.pojo;

import lombok.Data;

@Data
public class Url {
    private String httpPullUrl;
    private String hlsPullUrl;
    private String pushUrl;
    private String rtmpPullUrl;
}
