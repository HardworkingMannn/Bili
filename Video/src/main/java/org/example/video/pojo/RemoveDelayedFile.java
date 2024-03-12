package org.example.video.pojo;

import lombok.Data;

@Data
public class RemoveDelayedFile {
    private Integer userId;
    private String folder;
    private String filename;
    private Integer chunkTotal;
}
