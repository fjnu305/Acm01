package org.fjnu305.acm01.module.team.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamMemberLogEntity {

    private Long id;
    private Long teamPostId;
    private Long userId;
    private String action;
    private Long actorId;
    private LocalDateTime createdTime;
}
