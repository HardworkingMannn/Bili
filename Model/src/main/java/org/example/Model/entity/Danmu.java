package org.example.Model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("danmu")
public class  Danmu {
    @TableId
    private String danmuId;
    private String content;
    private Integer time;
    private String videoItemId;
}
