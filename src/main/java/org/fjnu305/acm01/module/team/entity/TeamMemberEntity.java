package org.fjnu305.acm01.module.team.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamMemberEntity {

    private Long id;
    private Long teamPostId;
    private Long userId;
    private String role;
    private Integer status;
    private String pendingKind;
    private LocalDateTime joinedTime;
    private LocalDateTime createdTime;
}
