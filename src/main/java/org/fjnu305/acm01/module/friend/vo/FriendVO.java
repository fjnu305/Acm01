package org.fjnu305.acm01.module.friend.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendVO {

    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String school;
    private Integer cfRating;
    private LocalDateTime friendsSince;
    private Boolean official;
}
