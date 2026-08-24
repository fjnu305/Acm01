package org.fjnu305.acm01.module.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSearchVO {

    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String school;
    /** NONE | PENDING_SENT | PENDING_RECEIVED | FRIENDS */
    private String friendStatus;
    private Boolean official;
}
