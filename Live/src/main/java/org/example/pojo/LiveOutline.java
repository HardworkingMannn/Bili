package org.example.pojo;

import lombok.Data;

@Data
public class LiveOutline {
    private String cid;
    private String name;
    private String image;
    private Integer userId;
    private String userImage;
    private String userName;
    private Integer watch;
    private Boolean isFollowed;
}
