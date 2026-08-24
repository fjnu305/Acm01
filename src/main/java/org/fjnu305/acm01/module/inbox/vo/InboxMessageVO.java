package org.fjnu305.acm01.module.inbox.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InboxMessageVO {

    private Long id;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    /** OFFICIAL / FRIEND / PERSONAL，由发件人与 ref 推导，不入库 */
    private String category;
    private String title;
    private String body;
    private String refType;
    private Long refId;
    private Boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdTime;
}
