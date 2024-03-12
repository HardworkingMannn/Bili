package org.example.Model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("everyday_likes")
public class EverydayLikes {
    private Integer everydayLikes;
    private Integer userId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
}
