package org.fjnu305.acm01.module.subscription.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ContestSubscriptionEntity {

    private Long id;
    private Long userId;
    private Long contestId;
    private Integer remindBeforeMinutes;
    private String channel;
    private Integer status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
