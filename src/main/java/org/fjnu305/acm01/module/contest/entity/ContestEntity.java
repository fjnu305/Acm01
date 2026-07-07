package org.fjnu305.acm01.module.contest.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统一赛事表 {@code contest} 实体。
 * <p>表结构见 {@code docs/module-2-contest-crawler.md §3.1}，唯一键 {@code uk_source_external(source, external_id)}。</p>
 */
@Data
public class ContestEntity {

    /** 赛事ID，自增主键；对外 API 使用此 id（模块 3 订阅外键） */
    private Long id;

    /** 来源平台，对应 {@link org.fjnu305.acm01.Common.enums.ContestSource}，列 source VARCHAR(32) */
    private String source;

    /** 平台原始 ID，与 source 组成唯一键，列 external_id VARCHAR(128) */
    private String externalId;

    /** 赛事名称，列 title VARCHAR(255) NOT NULL */
    private String title;

    /** 赛事描述，列 description TEXT */
    private String description;

    /** 报名/详情链接，列 url VARCHAR(500) */
    private String url;

    /** 开始时间，列 start_time DATETIME NOT NULL，索引 idx_start_time */
    private LocalDateTime startTime;

    /** 结束时间，列 end_time DATETIME */
    private LocalDateTime endTime;

    /** 报名开始，列 register_start DATETIME */
    private LocalDateTime registerStart;

    /** 报名截止，列 register_end DATETIME */
    private LocalDateTime registerEnd;

    /** 1即将开始 2进行中 3已结束，列 status TINYINT，索引 idx_status */
    private Integer status;

    /** 难度标签如 Div.2，列 difficulty VARCHAR(32) */
    private String difficulty;

    /** 赛事类型如 CF/ICPC，列 contest_type VARCHAR(32) */
    private String contestType;

    /** 线上/线下地点，列 location VARCHAR(100) */
    private String location;

    /** 原始数据 SHA-256，增量更新判断，列 raw_hash VARCHAR(64) */
    private String rawHash;

    /** 最后爬取时间，列 last_crawled_at DATETIME */
    private LocalDateTime lastCrawledAt;

    /** 创建时间，列 created_time，默认 CURRENT_TIMESTAMP */
    private LocalDateTime createdTime;

    /** 更新时间，列 updated_time，ON UPDATE CURRENT_TIMESTAMP */
    private LocalDateTime updatedTime;

    /** 逻辑删除 0否 1是，列 deleted TINYINT DEFAULT 0 */
    private Integer deleted;
}
