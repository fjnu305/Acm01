package org.fjnu305.acm01.module.social.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentVO {

    private Long id;
    private Long postId;
    private Long userId;
    private String authorNickname;
    private String authorAvatar;
    private String content;
    private Long parentId;
    private LocalDateTime createdTime;
}
