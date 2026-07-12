package org.fjnu305.acm01.module.notify.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotifyTaskEntity {

    private Long id;
    private Long subscriptionId;
    private String channel;
    private LocalDateTime scheduledAt;
    private String status;
    private LocalDateTime sentAt;
    private Integer retryCount;
    private String errorMessage;
    private String idempotentKey;
    private LocalDateTime createdTime;
}
