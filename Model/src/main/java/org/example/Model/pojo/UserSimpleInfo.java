package org.example.Model.pojo;

import lombok.Data;

@Data
public class UserSimpleInfo {
    private Integer userId;
    private String userName;
    private String userImage;
    private Integer fans;
    private String signature;
    private Boolean isFollow;
}
