package org.fjnu305.acm01.module.contest.crawl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 爬虫 HTTP 客户端配置项。
 * <p>
 * 绑定 {@code application.yml} 中 {@code contest.crawl.http.*} 前缀，
 * 由 {@link org.fjnu305.acm01.Config.OkHttpConfig} 与
 * {@link org.fjnu305.acm01.module.contest.crawl.common.http.CrawlHttpClient} 读取。
 * 各平台 Crawler 共用同一套超时 / 连接池 / 重试策略。
 * </p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "contest.crawl.http")
public class CrawlHttpProperties {

    /**
     * OkHttp 连接池最大空闲连接数。
     */
    private int maxIdleConnections = 5;

    private int keepAliveMinutes = 5;

    private int connectTimeoutSeconds = 10;

    private int readTimeoutSeconds = 30;

    private int writeTimeoutSeconds = 10;

    private int callTimeoutSeconds = 60;

    private int maxRetries = 3;

    private long retryBaseDelayMillis = 500;
}
