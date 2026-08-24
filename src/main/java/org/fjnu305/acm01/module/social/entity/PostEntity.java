package org.fjnu305.acm01.module.social.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostEntity {

    private Long id;
    private Long userId;
    private String content;
    private Long topicId;
    private Integer likeCount;
    private Integer commentCount;
    private Integer status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
