package org.fjnu305.acm01.module.solution.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SolutionTemplateEntity {

    private Long id;
    private String name;
    private String category;
    private String content;
    private Long createdBy;
    private LocalDateTime createdTime;
}
