package org.example.video.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("file_storage")
@AllArgsConstructor
@NoArgsConstructor
public class FileStorage {
    private String filename;
    private String fileType;
}
