package org.fjnu305.acm01.module.contest.crawler.http;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 爬虫 HTTP 客户端配置项。
 * <p>
 * 绑定 {@code application.yml} 中 {@code contest.crawl.http.*} 前缀，
 * 由 {@link org.fjnu305.acm01.Config.OkHttpConfig} 与 {@link CrawlHttpClient} 读取。
 * 各平台 Crawler 共用同一套超时 / 连接池 / 重试策略。
 * </p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "contest.crawl.http")
public class CrawlHttpProperties {

    /**
     * OkHttp 连接池最大空闲连接数。
     * <p>爬虫 Job 按平台顺序执行，单主机通常只需 1 条连接；5 个空闲连接足够复用 TCP 握手。</p>
     */
    private int maxIdleConnections = 5;

    /** 空闲连接在池中的保活时间（分钟），超时后关闭以释放资源 */
    private int keepAliveMinutes = 5;

    /** TCP 连接建立超时（秒） */
    private int connectTimeoutSeconds = 10;

    /** 等待响应体读取超时（秒）；Codeforces API 响应较大，适当放宽 */
    private int readTimeoutSeconds = 30;

    /** 请求体写入超时（秒），GET 场景基本不会触发，保留默认值即可 */
    private int writeTimeoutSeconds = 10;

    /** 单次 Call 整体超时（秒），含连接 + 读 + 重试等待的上限 */
    private int callTimeoutSeconds = 60;

    /**
     * 失败后重试次数（不含首次请求）。
     * <p>例如 {@code maxRetries=3} 表示最多尝试 4 次（1 次初始 + 3 次重试）。</p>
     */
    private int maxRetries = 3;

    /**
     * 重试基础退避间隔（毫秒）。
     * <p>第 n 次重试前等待 {@code retryBaseDelayMillis * 2^(n-1)}，避免瞬时打满目标站点。</p>
     */
    private long retryBaseDelayMillis = 500;
}
