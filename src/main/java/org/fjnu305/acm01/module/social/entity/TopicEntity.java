package org.fjnu305.acm01.module.social.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TopicEntity {

    private Long id;
    private String name;
    private String description;
    private Integer postCount;
    private LocalDateTime createdTime;
}
