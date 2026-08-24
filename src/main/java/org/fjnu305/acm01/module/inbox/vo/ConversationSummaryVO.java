package org.fjnu305.acm01.module.inbox.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationSummaryVO {

    private Long peerId;
    private String peerName;
    private String peerAvatar;
    private Boolean official;
    private String lastMessageBody;
    private String lastMessageTitle;
    private LocalDateTime lastMessageTime;
    private Long unreadCount;
}
