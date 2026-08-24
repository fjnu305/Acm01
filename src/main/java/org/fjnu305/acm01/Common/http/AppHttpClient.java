package org.fjnu305.acm01.Common.http;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared outbound HTTP client: GET/POST, retries, JSON encode/decode.
 * Callers pass per-request options via {@link HttpRequestOptions}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppHttpClient {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final Pattern HTTP_CODE_PATTERN = Pattern.compile("HTTP (\\d{3})");

    private static final ThreadLocal<Integer> LAST_ATTEMPT_COUNT = new ThreadLocal<>();

    private final OkHttpClient okHttpClient;
    private final AppHttpProperties properties;
    private final ObjectMapper objectMapper;

    public int getLastAttemptCount() {
        Integer count = LAST_ATTEMPT_COUNT.get();
        return count != null ? count : 1;
    }

    public void clearLastAttemptCount() {
        LAST_ATTEMPT_COUNT.remove();
    }

    public String get(String url) {
        return get(url, HttpRequestOptions.empty());
    }

    public String get(String url, HttpRequestOptions options) {
        return execute(buildGetRequest(url, options), options);
    }

    public String getHtml(String url, HttpRequestOptions options) {
        return get(url, mergeAccept(options, "text/html"));
    }

    public <T> T getJson(String url, Class<T> type) {
        return getJson(url, type, HttpRequestOptions.jsonGet());
    }

    public <T> T getJson(String url, Class<T> type, HttpRequestOptions options) {
        try {
            String body = get(url, mergeAccept(options, "application/json"));
            return objectMapper.readValue(body, type);
        } catch (HttpClientException e) {
            throw e;
        } catch (Exception e) {
            int maxRetries = resolveMaxRetries(options);
            throw HttpClientException.parseError(
                    url,
                    maxRetries,
                    getLastAttemptCount(),
                    "JSON parse failed: " + e.getMessage(),
                    e
            );
        }
    }

    public String postJson(String url, Object body) {
        return postJson(url, body, HttpRequestOptions.jsonPost());
    }

    public String postJson(String url, Object body, HttpRequestOptions options) {
        try {
            String json = objectMapper.writeValueAsString(body);
            Request request = buildPostRequest(url, json, options);
            return execute(request, options);
        } catch (HttpClientException e) {
            throw e;
        } catch (Exception e) {
            int maxRetries = resolveMaxRetries(options);
            throw HttpClientException.parseError(
                    url,
                    maxRetries,
                    getLastAttemptCount(),
                    "JSON encode failed: " + e.getMessage(),
                    e
            );
        }
    }

    public <T> T postJson(String url, Object body, Class<T> responseType, HttpRequestOptions options) {
        try {
            String responseBody = postJson(url, body, options);
            return objectMapper.readValue(responseBody, responseType);
        } catch (HttpClientException e) {
            throw e;
        } catch (Exception e) {
            int maxRetries = resolveMaxRetries(options);
            throw HttpClientException.parseError(
                    url,
                    maxRetries,
                    getLastAttemptCount(),
                    "JSON parse failed: " + e.getMessage(),
                    e
            );
        }
    }

    private String execute(Request request, HttpRequestOptions options) {
        clearLastAttemptCount();
        String url = request.url().toString();
        int maxRetries = resolveMaxRetries(options);
        int totalAttempts = maxRetries + 1;
        IOException lastException = null;
        Integer lastHttpStatus = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            if (attempt > 0) {
                sleepBeforeRetry(attempt, options);
            }

            try (Response response = okHttpClient.newCall(request).execute()) {
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
                    throw HttpClientException.httpError(
                            url,
                            code,
                            maxRetries,
                            attempt + 1,
                            "HTTP " + code + " from " + url + ": " + truncate(bodyText),
                            null
                    );
                }
                LAST_ATTEMPT_COUNT.set(attempt + 1);
                return bodyText;
            } catch (HttpClientException e) {
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
        throw HttpClientException.networkError(url, maxRetries, totalAttempts, message, lastException);
    }

    private Request buildGetRequest(String url, HttpRequestOptions options) {
        Request.Builder builder = new Request.Builder().url(url).get();
        applyHeaders(builder, options);
        return builder.build();
    }

    private Request buildPostRequest(String url, String jsonBody, HttpRequestOptions options) {
        RequestBody requestBody = RequestBody.create(jsonBody, JSON_MEDIA_TYPE);
        Request.Builder builder = new Request.Builder().url(url).post(requestBody);
        applyHeaders(builder, options);
        return builder.build();
    }

    private void applyHeaders(Request.Builder builder, HttpRequestOptions options) {
        String userAgent = options != null && options.userAgent() != null
                ? options.userAgent()
                : properties.getDefaultUserAgent();
        builder.header("User-Agent", userAgent);

        if (options != null && options.accept() != null) {
            builder.header("Accept", options.accept());
        }

        if (options != null && options.headers() != null) {
            for (Map.Entry<String, String> entry : options.headers().entrySet()) {
                builder.header(entry.getKey(), entry.getValue());
            }
        }
    }

    private int resolveMaxRetries(HttpRequestOptions options) {
        if (options != null && options.maxRetries() != null) {
            return Math.max(0, options.maxRetries());
        }
        return properties.getMaxRetries();
    }

    private long resolveRetryBaseDelay(HttpRequestOptions options) {
        if (options != null && options.retryBaseDelayMillis() != null) {
            return Math.max(0L, options.retryBaseDelayMillis());
        }
        return properties.getRetryBaseDelayMillis();
    }

    private static HttpRequestOptions mergeAccept(HttpRequestOptions options, String accept) {
        if (options == null) {
            return HttpRequestOptions.builder().accept(accept).build();
        }
        if (options.accept() != null) {
            return options;
        }
        return HttpRequestOptions.copyOf(options).accept(accept).build();
    }

    private boolean shouldRetry(int httpCode) {
        return httpCode == 429 || httpCode == 502 || httpCode == 503 || httpCode == 504;
    }

    private void sleepBeforeRetry(int attempt, HttpRequestOptions options) {
        long delayMs = resolveRetryBaseDelay(options) * (1L << (attempt - 1));
        try {
            TimeUnit.MILLISECONDS.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw HttpClientException.networkError(
                    null,
                    resolveMaxRetries(options),
                    attempt,
                    "Retry sleep interrupted",
                    e
            );
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
