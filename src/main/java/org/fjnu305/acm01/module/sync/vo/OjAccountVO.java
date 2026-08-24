package org.fjnu305.acm01.module.sync.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OjAccountVO {

    private Long id;
    private String platform;
    private String handle;
    private Integer currentRating;
    private Integer maxRating;
    private String rank;
    private LocalDateTime lastSyncAt;
    private boolean hasCredential;
}
