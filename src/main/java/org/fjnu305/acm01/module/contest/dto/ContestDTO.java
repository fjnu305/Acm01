package org.fjnu305.acm01.module.contest.dto;

import lombok.Builder;
import lombok.Data;
import org.fjnu305.acm01.Common.enums.ContestSource;

import java.time.LocalDateTime;

/**
 * 爬虫内部归一化数据传输对象。
 * <p>
 * 各平台 {@link org.fjnu305.acm01.module.contest.crawler.ContestCrawler} 解析原始数据后，
 * 必须统一转换为本结构，再交给 {@link org.fjnu305.acm01.module.contest.service.ContestCrawlService} 做去重与入库。
 * </p>
 * <p>注意：DTO 仅在爬虫 → Service 层内部流转，不直接暴露给前端 API。</p>
 */
@Data
@Builder
public class ContestDTO {

    /** 赛事来源平台，与 contest.source 字段对应 */
    private ContestSource source;

    /** 平台原始 ID，与 source 组成唯一键 uk_source_external */
    private String externalId;

    /** 赛事名称 */
    private String title;

    /** 赛事描述（可选） */
    private String description;

    /** 详情 / 报名链接 */
    private String url;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间（可选） */
    private LocalDateTime endTime;

    /** 报名开始时间（可选） */
    private LocalDateTime registerStart;

    /** 报名截止时间（可选） */
    private LocalDateTime registerEnd;

    /**
     * 赛事状态：1 即将开始，2 进行中，3 已结束。
     * 也可由 ContestStatusRefreshJob 根据时间批量刷新。
     */
    private Integer status;

    /** 难度标签，如 Div.1 / Div.2（可选） */
    private String difficulty;

    /** 赛事类型，如 ICPC / OI / 个人赛（可选） */
    private String contestType;

    /** 地点：Online 或具体城市（可选） */
    private String location;

    /**
     * 原始数据哈希，用于增量更新。
     * 关键字段变化时重新计算，相同则跳过 UPDATE。
     */
    private String rawHash;
}
