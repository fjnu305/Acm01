package org.fjnu305.acm01.module.social.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCommentRequest {

    @NotBlank(message = "Content is required")
    @Size(max = 1000, message = "Comment too long")
    private String content;

    private Long parentId;
}
