package org.example.Model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;

public interface Comment {
    String getParentId();
    Integer getFromType();
    String getContent();
    void setContent(String content);
}
