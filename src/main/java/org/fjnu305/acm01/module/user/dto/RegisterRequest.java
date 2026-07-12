package org.fjnu305.acm01.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 32, message = "Password must be 6-32 characters")
    private String password;

    @Size(max = 50, message = "Nickname must be at most 50 characters")
    private String nickname;

    @Size(max = 100, message = "Email must be at most 100 characters")
    private String email;
}
