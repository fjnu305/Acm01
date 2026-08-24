package org.fjnu305.acm01.module.friend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendshipEntity {

    private Long id;
    private Long userLowId;
    private Long userHighId;
    private LocalDateTime createdTime;
}
