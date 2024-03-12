package org.example.pojo;

import lombok.Data;

@Data
public class RequestResult {
    private String ret;
    private Integer code;
    private String requestId;
}
