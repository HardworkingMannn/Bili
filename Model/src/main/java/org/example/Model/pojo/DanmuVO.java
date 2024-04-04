package org.example.Model.pojo;

import lombok.Data;

import java.util.List;

@Data
public class DanmuVO {
    private Integer requestId;
    private Integer timestamp;
    private String videoId;
    private List<String> danmuList;
}
