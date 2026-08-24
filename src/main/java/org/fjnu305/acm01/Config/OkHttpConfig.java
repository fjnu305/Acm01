package org.fjnu305.acm01.Config;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.fjnu305.acm01.Common.http.AppHttpProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 全局 OkHttp 客户端：供 {@link org.fjnu305.acm01.Common.http.AppHttpClient} 注入复用。
 */
@Configuration
public class OkHttpConfig {

    @Bean
    public ConnectionPool appConnectionPool(AppHttpProperties properties) {
        return new ConnectionPool(
                properties.getMaxIdleConnections(),
                properties.getKeepAliveMinutes(),
                TimeUnit.MINUTES
        );
    }

    /**
     * {@code retryOnConnectionFailure(true)} 仅处理连接层瞬断；
     * 应用层 HTTP 5xx / 429 重试由 {@link org.fjnu305.acm01.Common.http.AppHttpClient} 负责。
     */
    @Bean
    public OkHttpClient okHttpClient(AppHttpProperties properties, ConnectionPool appConnectionPool) {
        return new OkHttpClient.Builder()
                .connectionPool(appConnectionPool)
                .connectTimeout(properties.getConnectTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(properties.getReadTimeoutSeconds(), TimeUnit.SECONDS)
                .writeTimeout(properties.getWriteTimeoutSeconds(), TimeUnit.SECONDS)
                .callTimeout(properties.getCallTimeoutSeconds(), TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }
}
