package org.fjnu305.acm01.module.user.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminUserVO {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private Integer status;
    private List<String> roles;
    private LocalDateTime createdTime;
    private LocalDateTime lastLoginTime;
}
