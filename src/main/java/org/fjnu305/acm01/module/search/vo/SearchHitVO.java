package org.fjnu305.acm01.module.search.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SearchHitVO {

    private String type;
    private Long refId;
    private String title;
    private String snippet;
    private String tags;
    private String authorName;
    private LocalDateTime createdTime;
    private List<String> highlights;
}
