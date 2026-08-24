package org.fjnu305.acm01.module.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 50, message = "Nickname must be at most 50 characters")
    private String nickname;

    @Size(max = 100, message = "Email must be at most 100 characters")
    private String email;

    @Size(max = 100, message = "School must be at most 100 characters")
    private String school;

    @Size(max = 500, message = "Bio must be at most 500 characters")
    private String bio;
}
