package org.fjnu305.acm01.module.notify.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotifyLogEntity {

    private Long id;
    private Long notifyTaskId;
    private Long userId;
    private String channel;
    private String target;
    private String status;
    private String providerMsgId;
    private String errorMessage;
    private LocalDateTime createdTime;
}
