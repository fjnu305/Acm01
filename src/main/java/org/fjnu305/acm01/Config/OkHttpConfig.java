package org.fjnu305.acm01.Config;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.fjnu305.acm01.module.contest.crawl.config.CrawlHttpProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 爬虫模块 OkHttp 客户端配置?
 * <p>
 * 注册全局单例 {@link OkHttpClient} ?{@link ConnectionPool}?
 * ?{@link org.fjnu305.acm01.module.contest.crawl.common.http.CrawlHttpClient} 注入复用?
 * </p>
 */
@Configuration
public class OkHttpConfig {

    /**
     * 爬虫专用连接?Bean?
     */
    @Bean
    public ConnectionPool crawlConnectionPool(CrawlHttpProperties properties) {
        return new ConnectionPool(
                properties.getMaxIdleConnections(),
                properties.getKeepAliveMinutes(),
                TimeUnit.MINUTES
        );
    }

    /**
     * 爬虫专用 OkHttp 客户?Bean?
     * <p>
     * {@code retryOnConnectionFailure(true)} 仅处理连接层瞬断?
     * 应用?HTTP 5xx / 429 重试?{@link org.fjnu305.acm01.module.contest.crawl.common.http.CrawlHttpClient} 负责?
     * </p>
     */
    @Bean
    public OkHttpClient crawlOkHttpClient(CrawlHttpProperties properties, ConnectionPool crawlConnectionPool) {
        return new OkHttpClient.Builder()
                .connectionPool(crawlConnectionPool)
                .connectTimeout(properties.getConnectTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(properties.getReadTimeoutSeconds(), TimeUnit.SECONDS)
                .writeTimeout(properties.getWriteTimeoutSeconds(), TimeUnit.SECONDS)
                .callTimeout(properties.getCallTimeoutSeconds(), TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }
}
