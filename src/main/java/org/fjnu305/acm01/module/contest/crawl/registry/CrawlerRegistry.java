package org.fjnu305.acm01.module.contest.crawl.registry;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawler.ContestCrawler;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 平台爬虫策略注册表：按 {@link ContestSource} 查找 {@link ContestCrawler} 实现。
 */
@Component
@RequiredArgsConstructor
public class CrawlerRegistry {

    private final List<ContestCrawler> crawlers;
    private Map<ContestSource, ContestCrawler> index;

    @PostConstruct
    void init() {
        index = new EnumMap<>(ContestSource.class);
        for (ContestCrawler crawler : crawlers) {
            index.putIfAbsent(crawler.getSource(), crawler);
        }
    }

    public Collection<ContestCrawler> all() {
        return crawlers;
    }

    public ContestCrawler getRequired(ContestSource source) {
        ContestCrawler crawler = index.get(source);
        if (crawler == null) {
            throw new IllegalArgumentException("未找到爬虫策略: " + source.getValue());
        }
        return crawler;
    }
}
