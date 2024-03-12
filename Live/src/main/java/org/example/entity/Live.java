package org.example.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("live")
public class Live {
    @TableId
    private String cid;
    private String name;
    private String image;
    private Integer userId;
    private Integer type;
    private String pushUrl;
    private String httpPullUrl;  //http拉流
    private String hlsPullUrl;  //hls拉流
    private String rtmpPullUrl; //rtmp拉流
    private Boolean isLive;
}
