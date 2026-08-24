package org.fjnu305.acm01.module.friend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendFriendRequestDTO {

    private Long targetUserId;

    @Size(max = 200)
    private String message;
}
