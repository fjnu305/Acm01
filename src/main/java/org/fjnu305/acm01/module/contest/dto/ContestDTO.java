package org.fjnu305.acm01.module.contest.dto;

import lombok.Builder;
import lombok.Data;
import org.fjnu305.acm01.Common.enums.ContestSource;

import java.time.LocalDateTime;

/**
 */
@Data
@Builder
public class ContestDTO {

    private ContestSource source;

    private String externalId;

    /** ?contest.title NOT NULL */
    private String title;

    /** ?contest.description */
    private String description;

    /** ?contest.url */
    private String url;

    /** ?contest.start_time NOT NULL */
    private LocalDateTime startTime;

    /** ?contest.end_time */
    private LocalDateTime endTime;

    /** ?contest.register_start */
    private LocalDateTime registerStart;

    /** ?contest.register_end */
    private LocalDateTime registerEnd;

    /** ?contest.status?即将开?2进行?3已结?*/
    private Integer status;

    /** ?contest.difficulty */
    private String difficulty;

    /** ?contest.contest_type */
    private String contestType;

    /** ?contest.location */
    private String location;

    private String rawHash;
}
