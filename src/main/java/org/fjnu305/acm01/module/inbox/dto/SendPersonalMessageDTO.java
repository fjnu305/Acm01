package org.fjnu305.acm01.module.inbox.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendPersonalMessageDTO {

    private Long recipientId;

    /** 可选，聊天消息通常留空 */
    @Size(max = 200)
    private String title;

    @Size(max = 2000)
    private String body;
}
