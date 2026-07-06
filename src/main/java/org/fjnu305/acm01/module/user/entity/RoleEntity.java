package org.fjnu305.acm01.module.user.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoleEntity {

    private Long id;
    private String roleCode;
    private String roleName;
    private Integer status;
    private LocalDateTime createdTime;
}
