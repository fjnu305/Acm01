package org.fjnu305.acm01.module.social.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LikeEntity {

    private Long id;
    private Long postId;
    private Long userId;
    private LocalDateTime createdTime;
}
