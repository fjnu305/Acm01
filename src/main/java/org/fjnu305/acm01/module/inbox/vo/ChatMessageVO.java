package org.fjnu305.acm01.module.inbox.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageVO {

    private Long id;
    private Long senderId;
    private Long recipientId;
    private String senderName;
    private String senderAvatar;
    private String title;
    private String body;
    private String refType;
    private Long refId;
    /** 是否为当前查看者发送 */
    private Boolean mine;
    private Boolean read;
    private LocalDateTime createdTime;
}
