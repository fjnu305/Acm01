package org.fjnu305.acm01.Common.http;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Global HTTP client settings shared by crawl, AI, and other outbound calls. */
@Data
@Component
@ConfigurationProperties(prefix = "contest.crawl.http")
public class AppHttpProperties {

    private int maxIdleConnections = 5;

    private int keepAliveMinutes = 5;

    private int connectTimeoutSeconds = 10;

    private int readTimeoutSeconds = 30;

    private int writeTimeoutSeconds = 10;

    private int callTimeoutSeconds = 60;

    private int maxRetries = 3;

    private long retryBaseDelayMillis = 500;

    private String defaultUserAgent = "Acm01/1.0";
}
