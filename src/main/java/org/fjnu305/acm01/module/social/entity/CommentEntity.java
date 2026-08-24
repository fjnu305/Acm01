package org.fjnu305.acm01.module.social.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentEntity {

    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private Long parentId;
    private Integer status;
    private LocalDateTime createdTime;
}
