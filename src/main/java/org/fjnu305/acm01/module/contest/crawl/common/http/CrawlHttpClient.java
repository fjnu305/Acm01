package org.fjnu305.acm01.module.contest.crawl.common.http;

import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.fjnu305.acm01.module.contest.crawl.config.CrawlHttpProperties;
import org.fjnu305.acm01.module.contest.crawl.exception.CrawlFetchException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 爬虫 HTTP 工具类。
 * <p>
 * 供全平台策略统一写入 {@code contest_crawl_log}。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlHttpClient {

    private static final String DEFAULT_USER_AGENT =
            "Acm01-ContestCrawler/1.0 (+https://github.com/fjnu305/Acm01)";

    private static final Pattern HTTP_CODE_PATTERN = Pattern.compile("HTTP (\\d{3})");

    private static final ThreadLocal<Integer> LAST_ATTEMPT_COUNT = new ThreadLocal<>();

    private final OkHttpClient crawlOkHttpClient;
    private final CrawlHttpProperties properties;
    private final ObjectMapper objectMapper;

    public int getLastAttemptCount() {
        Integer count = LAST_ATTEMPT_COUNT.get();
        return count != null ? count : 1;
    }

    public void clearLastAttemptCount() {
        LAST_ATTEMPT_COUNT.remove();
    }

    public String getBody(String url) {
        return getBody(url, "application/json");
    }

    public String getHtml(String url) {
        return getBody(url, "text/html");
    }

    private String getBody(String url, String accept) {
        clearLastAttemptCount();
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", DEFAULT_USER_AGENT)
                .header("Accept", accept)
                .get()
                .build();

        int maxRetries = properties.getMaxRetries();
        int totalAttempts = maxRetries + 1;
        IOException lastException = null;
        Integer lastHttpStatus = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            if (attempt > 0) {
                sleepBeforeRetry(attempt);
            }

            try (Response response = crawlOkHttpClient.newCall(request).execute()) {
                int code = response.code();
                if (shouldRetry(code) && attempt < maxRetries) {
                    lastHttpStatus = code;
                    log.warn("HTTP {} from {}, retry {}/{}", code, url, attempt + 1, maxRetries);
                    continue;
                }

                ResponseBody body = response.body();
                String bodyText = body != null ? body.string() : "";

                if (!response.isSuccessful()) {
                    lastHttpStatus = code;
                    LAST_ATTEMPT_COUNT.set(attempt + 1);
                    throw CrawlFetchException.httpError(
                            url, code, maxRetries, attempt + 1,
                            "HTTP " + code + " from " + url + ": " + truncate(bodyText),
                            null
                    );
                }
                LAST_ATTEMPT_COUNT.set(attempt + 1);
                return bodyText;
            } catch (CrawlFetchException e) {
                throw e;
            } catch (IOException e) {
                lastException = e;
                if (attempt < maxRetries) {
                    log.warn("Request failed (attempt {}/{}): {} - {}",
                            attempt + 1, totalAttempts, url, e.getMessage());
                }
            }
        }

        Integer httpStatus = lastHttpStatus != null ? lastHttpStatus : parseHttpStatus(lastException);
        String message = lastException != null
                ? lastException.getMessage()
                : "Request failed after " + totalAttempts + " attempts";
        LAST_ATTEMPT_COUNT.set(totalAttempts);
        throw CrawlFetchException.httpError(url, httpStatus, maxRetries, totalAttempts, message, lastException);
    }

    public <T> T getJson(String url, Class<T> type) {
        try {
            String body = getBody(url);
            return objectMapper.readValue(body, type);
        } catch (CrawlFetchException e) {
            throw e;
        } catch (Exception e) {
            throw CrawlFetchException.parseError(
                    url,
                    properties.getMaxRetries(),
                    1,
                    "JSON parse failed: " + e.getMessage(),
                    e
            );
        }
    }

    private boolean shouldRetry(int httpCode) {
        return httpCode == 429 || httpCode == 502 || httpCode == 503 || httpCode == 504;
    }

    private void sleepBeforeRetry(int attempt) {
        long delayMs = properties.getRetryBaseDelayMillis() * (1L << (attempt - 1));
        try {
            TimeUnit.MILLISECONDS.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw CrawlFetchException.unknown(
                    null, properties.getMaxRetries(), "Retry sleep interrupted", e);
        }
    }

    private static Integer parseHttpStatus(IOException e) {
        if (e == null || e.getMessage() == null) {
            return null;
        }
        Matcher matcher = HTTP_CODE_PATTERN.matcher(e.getMessage());
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }

    private static String truncate(String text) {
        if (text == null || text.length() <= 200) {
            return text;
        }
        return text.substring(0, 200) + "...";
    }
}
