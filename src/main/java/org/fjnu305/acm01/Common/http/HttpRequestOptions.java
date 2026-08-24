package org.fjnu305.acm01.Common.http;

import lombok.Builder;
import lombok.Singular;

import java.util.Collections;
import java.util.Map;

/** Per-request HTTP options (headers, retries, user agent, etc.). */
@Builder
public record HttpRequestOptions(
        String accept,
        @Singular("header") Map<String, String> headers,
        Integer maxRetries,
        Long retryBaseDelayMillis,
        String userAgent
) {

    public HttpRequestOptions {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
    }

    public static HttpRequestOptions empty() {
        return HttpRequestOptions.builder().build();
    }

    public static HttpRequestOptions jsonGet() {
        return HttpRequestOptions.builder().accept("application/json").build();
    }

    public static HttpRequestOptions htmlGet() {
        return HttpRequestOptions.builder().accept("text/html").build();
    }

    public static HttpRequestOptions jsonPost() {
        return HttpRequestOptions.builder()
                .accept("application/json")
                .header("Content-Type", "application/json")
                .build();
    }

    public static HttpRequestOptions.HttpRequestOptionsBuilder copyOf(HttpRequestOptions source) {
        HttpRequestOptions.HttpRequestOptionsBuilder builder = HttpRequestOptions.builder()
                .accept(source.accept())
                .maxRetries(source.maxRetries())
                .retryBaseDelayMillis(source.retryBaseDelayMillis())
                .userAgent(source.userAgent());
        if (source.headers() != null) {
            source.headers().forEach(builder::header);
        }
        return builder;
    }
}
