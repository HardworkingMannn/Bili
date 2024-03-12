package org.example.video.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("file_belong")
public class FileBelong {
    @TableId
    private Integer userId;
    private String filename;
    private Boolean status;   //默认为false，表示没有完成传输
}
