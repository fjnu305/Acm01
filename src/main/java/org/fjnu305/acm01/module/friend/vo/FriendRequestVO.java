package org.fjnu305.acm01.module.friend.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendRequestVO {

    private Long id;
    private Long requesterId;
    private String requesterName;
    private String requesterAvatar;
    private Long addresseeId;
    private String addresseeName;
    private String addresseeAvatar;
    private String message;
    private Integer status;
    private LocalDateTime createdTime;
}
