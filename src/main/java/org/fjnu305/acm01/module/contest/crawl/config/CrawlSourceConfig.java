package org.fjnu305.acm01.module.contest.crawl.config;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.mapper.ContestSourceMapper;
import org.springframework.stereotype.Component;

/**
 * 爬虫源开关：唯一读取 {@code contest_source.crawl_enabled} 的地方。
 */
@Component
@RequiredArgsConstructor
public class CrawlSourceConfig {

    private final ContestSourceMapper contestSourceMapper;

    /**
     * 是否允许爬取该平台。
     * <p>库中无配置行时默认启用，便于本地开发。</p>
     */
    public boolean isCrawlEnabled(ContestSource source) {
        Integer enabled = contestSourceMapper.selectCrawlEnabled(source.getValue());
        return enabled == null || enabled == 1;
    }
}
