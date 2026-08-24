package org.fjnu305.acm01.Common.http;

import lombok.Getter;

/** Unified outbound HTTP failure (network, HTTP status, or parse errors). */
@Getter
public class HttpClientException extends RuntimeException {

    private final String url;
    private final Integer httpStatus;
    private final int maxRetries;
    private final int totalAttempts;
    private final Kind kind;

    public enum Kind {
        HTTP_ERROR,
        NETWORK_ERROR,
        PARSE_ERROR
    }

    public HttpClientException(Kind kind,
                               String url,
                               Integer httpStatus,
                               int maxRetries,
                               int totalAttempts,
                               String message,
                               Throwable cause) {
        super(message, cause);
        this.kind = kind;
        this.url = url;
        this.httpStatus = httpStatus;
        this.maxRetries = maxRetries;
        this.totalAttempts = totalAttempts;
    }

    public static HttpClientException httpError(String url,
                                                int httpStatus,
                                                int maxRetries,
                                                int totalAttempts,
                                                String message,
                                                Throwable cause) {
        return new HttpClientException(Kind.HTTP_ERROR, url, httpStatus, maxRetries, totalAttempts, message, cause);
    }

    public static HttpClientException networkError(String url,
                                                   int maxRetries,
                                                   int totalAttempts,
                                                   String message,
                                                   Throwable cause) {
        return new HttpClientException(Kind.NETWORK_ERROR, url, null, maxRetries, totalAttempts, message, cause);
    }

    public static HttpClientException parseError(String url,
                                                 int maxRetries,
                                                 int totalAttempts,
                                                 String message,
                                                 Throwable cause) {
        return new HttpClientException(Kind.PARSE_ERROR, url, null, maxRetries, totalAttempts, message, cause);
    }
}
