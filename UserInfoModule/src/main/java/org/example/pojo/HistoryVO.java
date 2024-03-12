package org.example.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HistoryVO {
    private String videoId;
    private String coverImageLink;
    private String title;

    private Integer totalHours;
    private Integer totalMinutes;
    private Integer totalSeconds;

    private String name;
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime time;
}
