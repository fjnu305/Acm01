package org.fjnu305.acm01.module.team.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamMemberVO {

    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private Integer cfRating;
    private String school;
    private String role;
    private Integer status;
    private String pendingKind;
    private LocalDateTime joinedTime;
}
