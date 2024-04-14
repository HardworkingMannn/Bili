package org.example.video.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data

public class VideoChunkVO {
    private Integer type;   //类型，默认为0成功,1为文件失效，2为块数不正确
    @Schema(description = "文件名，当传输第一个分块之后会回传，之后所有传输都需要带上这个文件名")
    private String filename;
    @Schema(description = "文件路径，传输完成最后一个分块之后返回，其余不返回")
    private String path;
    private Integer hours;
    private Integer minutes;
    private Integer seconds;
    private Integer processIndex;
    private Integer totalProcess;
}
