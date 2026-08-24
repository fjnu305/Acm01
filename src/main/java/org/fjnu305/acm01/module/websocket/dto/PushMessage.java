package org.fjnu305.acm01.module.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushMessage {

    private String type;
    private String title;
    private String body;
    private Long contestId;
    private String contestTitle;
    private String contestUrl;
    private Long inboxId;
    private String inboxCategory;
    private Long inboxSenderId;
    private String inboxRefType;
}
