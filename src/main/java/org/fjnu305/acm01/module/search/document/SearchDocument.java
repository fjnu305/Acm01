package org.fjnu305.acm01.module.search.document;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SearchDocument {

    private String id;
    private String type;
    private Long refId;
    private String title;
    private String content;
    private String tags;
    private Long authorId;
    private String authorName;
    private LocalDateTime createdTime;
}
