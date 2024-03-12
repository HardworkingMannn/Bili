package org.example.video.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RemoveFile {
    private String folder;
    private String filename;
    private Integer chunkTotal;
    private String extension;
}
