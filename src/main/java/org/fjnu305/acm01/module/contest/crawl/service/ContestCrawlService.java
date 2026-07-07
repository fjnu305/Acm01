package org.fjnu305.acm01.module.contest.crawl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.Common.enums.CrawlTriggerType;
import org.fjnu305.acm01.module.contest.crawl.config.CrawlSourceConfig;
import org.fjnu305.acm01.module.contest.crawl.pipeline.CrawlPipeline;
import org.fjnu305.acm01.module.contest.crawl.registry.CrawlerRegistry;
import org.fjnu305.acm01.module.contest.crawler.ContestCrawler;
import org.fjnu305.acm01.module.contest.dto.ContestCrawlResult;
import org.fjnu305.acm01.module.contest.dto.ContestPersistResult;
import org.springframework.stereotype.Service;

/**
 * 爬虫触发入口：校验开关 → 查找策略 → 委托 {@link CrawlPipeline}。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContestCrawlService {

    private final CrawlerRegistry crawlerRegistry;
    private final CrawlPipeline crawlPipeline;
    private final CrawlSourceConfig sourceConfig;

    public void crawlAll(CrawlTriggerType triggerType) {
        for (ContestCrawler crawler : crawlerRegistry.all()) {
            if (sourceConfig.isCrawlEnabled(crawler.getSource())) {
                crawl(crawler.getSource(), triggerType);
            }
        }
    }

    public void crawlAll() {
        crawlAll(CrawlTriggerType.AUTO);
    }

    public ContestCrawlResult crawl(ContestSource source, CrawlTriggerType triggerType) {
        if (!sourceConfig.isCrawlEnabled(source)) {
            log.debug("[{}] contest_source.crawl_enabled=0，跳过", source.getValue());
            return ContestCrawlResult.builder()
                    .source(source)
                    .triggerType(triggerType)
                    .elapsedMs(0)
                    .persist(ContestPersistResult.builder().build())
                    .build();
        }
        ContestCrawler crawler = crawlerRegistry.getRequired(source);
        return crawlPipeline.execute(crawler, triggerType);
    }

    public ContestCrawlResult crawlManual(ContestSource source) {
        return crawl(source, CrawlTriggerType.MANUAL);
    }
}
