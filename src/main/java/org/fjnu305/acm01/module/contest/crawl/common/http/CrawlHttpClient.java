package org.fjnu305.acm01.module.contest.crawl.common.http;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.Common.http.AppHttpClient;
import org.fjnu305.acm01.Common.http.HttpClientException;
import org.fjnu305.acm01.Common.http.HttpRequestOptions;
import org.fjnu305.acm01.module.contest.crawl.common.rate.CrawlRateLimiter;
import org.fjnu305.acm01.module.contest.crawl.exception.CrawlFetchException;
import org.springframework.stereotype.Component;

/**
 * 爬虫出站 HTTP 门面：限流 + 爬虫异常映射，底层统一走 {@link AppHttpClient}。
 */
@Component
@RequiredArgsConstructor
public class CrawlHttpClient {

    private static final String CRAWLER_USER_AGENT =
            "Acm01-ContestCrawler/1.0 (+https://github.com/fjnu305/Acm01)";

    private final AppHttpClient appHttpClient;
    private final CrawlRateLimiter crawlRateLimiter;

    public int getLastAttemptCount() {
        return appHttpClient.getLastAttemptCount();
    }

    public void clearLastAttemptCount() {
        appHttpClient.clearLastAttemptCount();
    }

    public String getBody(String url) {
        return getBody(url, "application/json", null);
    }

    public String getBody(String url, ContestSource source) {
        return getBody(url, "application/json", source);
    }

    public String getHtml(String url) {
        return getBody(url, "text/html", null);
    }

    public String getHtml(String url, ContestSource source) {
        return getBody(url, "text/html", source);
    }

    public <T> T getJson(String url, Class<T> type) {
        try {
            return appHttpClient.getJson(url, type, crawlOptions("application/json"));
        } catch (HttpClientException e) {
            throw toCrawlException(e);
        }
    }

    private String getBody(String url, String accept, ContestSource source) {
        if (source != null) {
            crawlRateLimiter.acquire(source);
        }
        try {
            return appHttpClient.get(url, crawlOptions(accept));
        } catch (HttpClientException e) {
            throw toCrawlException(e);
        }
    }

    private HttpRequestOptions crawlOptions(String accept) {
        return HttpRequestOptions.builder()
                .accept(accept)
                .userAgent(CRAWLER_USER_AGENT)
                .build();
    }

    private CrawlFetchException toCrawlException(HttpClientException e) {
        return switch (e.getKind()) {
            case HTTP_ERROR -> CrawlFetchException.httpError(
                    e.getUrl(),
                    e.getHttpStatus(),
                    e.getMaxRetries(),
                    e.getTotalAttempts(),
                    e.getMessage(),
                    e.getCause()
            );
            case PARSE_ERROR -> CrawlFetchException.parseError(
                    e.getUrl(),
                    e.getMaxRetries(),
                    e.getTotalAttempts(),
                    e.getMessage(),
                    e.getCause()
            );
            case NETWORK_ERROR -> CrawlFetchException.httpError(
                    e.getUrl(),
                    e.getHttpStatus(),
                    e.getMaxRetries(),
                    e.getTotalAttempts(),
                    e.getMessage(),
                    e.getCause()
            );
        };
    }
}
