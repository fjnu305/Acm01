package org.fjnu305.acm01.module.contest.crawl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 赛事爬虫 Quartz 调度配置，绑定 {@code contest.crawl.schedule.*}。
 */
@Data
@Component
@ConfigurationProperties(prefix = "contest.crawl.schedule")
public class CrawlScheduleProperties {

    /**
     * 是否启用定时自动爬取。
     */
    private boolean enabled = true;

    /**
     * Quartz cron 表达式，默认每 10 分钟（联调用）；上线可改为更长间隔。
     */
    private String cron = "0 */10 * * * ?";
}
