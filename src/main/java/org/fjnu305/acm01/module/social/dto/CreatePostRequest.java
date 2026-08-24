package org.fjnu305.acm01.module.social.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePostRequest {

    @NotBlank(message = "Content is required")
    @Size(max = 5000, message = "Content too long")
    private String content;

    private Long topicId;
}
