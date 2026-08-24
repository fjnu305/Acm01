package org.fjnu305.acm01.module.friend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendRequestEntity {

    private Long id;
    private Long requesterId;
    private Long addresseeId;
    private String message;
    private Integer status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
