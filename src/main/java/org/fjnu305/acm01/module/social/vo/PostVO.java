package org.fjnu305.acm01.module.social.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PostVO {

    private Long id;
    private Long userId;
    private String authorNickname;
    private String authorAvatar;
    private String content;
    private Long topicId;
    private String topicName;
    private Integer likeCount;
    private Integer commentCount;
    private boolean likedByMe;
    private LocalDateTime createdTime;
}
