package org.fjnu305.acm01.module.social.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FollowEntity {

    private Long id;
    private Long followerId;
    private Long followeeId;
    private LocalDateTime createdTime;
}
