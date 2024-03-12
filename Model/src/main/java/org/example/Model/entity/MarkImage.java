package org.example.Model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("mark_image")
public class MarkImage {
    private String markId;
    private String markImage;
}
