package org.fjnu305.acm01.module.contest.dto;

import lombok.Builder;
import lombok.Data;
import org.fjnu305.acm01.Common.enums.ContestSource;

import java.time.LocalDateTime;

/**
 * 爬虫归一化 DTO，字段与 {@code contest} 表业务列一一对应（见 module-2-contest-crawler.md §3.1）。
 * <p>各平台 Crawler 输出本结构，由 {@link org.fjnu305.acm01.module.contest.crawl.service.ContestPersistService} 转 Entity 入库。</p>
 */
@Data
@Builder
public class ContestDTO {

    /** → contest.source，ContestSource 枚举 value */
    private ContestSource source;

    /** → contest.external_id，与 source 组成 uk_source_external */
    private String externalId;

    /** → contest.title NOT NULL */
    private String title;

    /** → contest.description */
    private String description;

    /** → contest.url */
    private String url;

    /** → contest.start_time NOT NULL */
    private LocalDateTime startTime;

    /** → contest.end_time */
    private LocalDateTime endTime;

    /** → contest.register_start */
    private LocalDateTime registerStart;

    /** → contest.register_end */
    private LocalDateTime registerEnd;

    /** → contest.status，1即将开始 2进行中 3已结束 */
    private Integer status;

    /** → contest.difficulty */
    private String difficulty;

    /** → contest.contest_type */
    private String contestType;

    /** → contest.location */
    private String location;

    /** → contest.raw_hash，增量更新判断 */
    private String rawHash;
}
