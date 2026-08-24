package org.fjnu305.acm01.module.inbox.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InboxMessageEntity {

    private Long id;
    private Long recipientId;
    private Long senderId;
    private String title;
    private String body;
    private String refType;
    private Long refId;
    private LocalDateTime readAt;
    private LocalDateTime createdTime;
}
