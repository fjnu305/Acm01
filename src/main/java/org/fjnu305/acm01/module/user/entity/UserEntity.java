package org.fjnu305.acm01.module.user.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserEntity {

    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String avatar;
    private Integer gender;
    private String school;
    private String bio;
    private String cfHandle;
    private String atcoderHandle;
    private String nowcoderHandle;
    private String luoguHandle;
    private Integer cfRating;
    private Integer solvedCount;
    private Integer acCount;
    private Integer contestCount;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Integer deleted;
}
