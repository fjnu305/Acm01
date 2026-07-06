package org.fjnu305.acm01.module.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserInfoVO {

    private Long userId;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private String school;
    private String bio;
    private Integer cfRating;
    private Integer solvedCount;
    private Integer acCount;
    private Integer contestCount;
    private List<String> roles;
}
