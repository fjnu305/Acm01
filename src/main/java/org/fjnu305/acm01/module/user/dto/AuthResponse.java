package org.fjnu305.acm01.module.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String token;
    private UserInfoVO user;
}
