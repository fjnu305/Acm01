package org.fjnu305.acm01.module.solution.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SolutionUpdateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title too long")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @Size(max = 32)
    private String problemSource;

    @Size(max = 128)
    private String problemId;

    @Size(max = 500)
    private String tags;

    private Integer status = 1;
}
