package org.example.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EditInfo {
    private String userName;
    private String signature;
    @Schema(description = "性别，0为男，1为女，2为保密")
    private String sexType;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
    private String school;
}
