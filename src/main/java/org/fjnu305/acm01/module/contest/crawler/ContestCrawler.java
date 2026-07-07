package org.fjnu305.acm01.module.contest.crawler;

import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawler.exception.CrawlFetchException;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;

import java.util.List;

/**
 * 赛事爬虫策略接口：各平台只实现「如何拉取并映射为 {@link ContestDTO}」。
 * <p>调度、入库、日志见 {@code org.fjnu305.acm01.module.contest.crawl} 包。</p>
 */
public interface ContestCrawler {

    ContestSource getSource();

    String getPrimaryRequestUrl();

    /**
     * 拉取并解析赛事；失败抛 {@link CrawlFetchException}。
     */
    List<ContestDTO> fetchContests();
}
