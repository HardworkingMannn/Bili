package org.example.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EnterLiveVO {
    private Integer userId;
    private String userImage;
    private String userName;
    @Schema(description = "粉丝数")
    private Integer fans;
    private Boolean isFollow;

    private String name;
    private String httpPullUrl;
    private String hlsPullUrl;
    private String rtmpPullUrl;
}
