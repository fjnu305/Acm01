package org.fjnu305.acm01.module.contest.dto;

import lombok.Builder;
import lombok.Data;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.Common.enums.CrawlTriggerType;

/**
 * 单次爬虫执行完整结果：爬取 + contest 表入库 + contest_crawl_log 已异步写入。
 */
@Data
@Builder
public class ContestCrawlResult {

    /** 平台，对应 contest.source / contest_source.source_code */
    private ContestSource source;

    /** 触发方式，对应 contest_crawl_log.trigger_type */
    private CrawlTriggerType triggerType;

    /** 总耗时毫秒，对应 contest_crawl_log.elapsed_ms */
    private long elapsedMs;

    /** contest 表 insert/update/skip 统计 */
    private ContestPersistResult persist;
}
