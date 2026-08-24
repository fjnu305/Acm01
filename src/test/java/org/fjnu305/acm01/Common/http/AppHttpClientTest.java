package org.fjnu305.acm01.Common.http;

import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppHttpClientTest {

    private AppHttpClient client;

    @BeforeEach
    void setUp() {
        AppHttpProperties properties = new AppHttpProperties();
        properties.setMaxRetries(0);
        client = new AppHttpClient(new OkHttpClient(), properties, new ObjectMapper());
    }

    @Test
    void postJson_serializesBodyAndParsesResponse() {
        try (var server = new okhttp3.mockwebserver.MockWebServer()) {
            server.enqueue(new okhttp3.mockwebserver.MockResponse()
                    .setResponseCode(200)
                    .setBody("{\"reply\":\"ok\"}")
                    .addHeader("Content-Type", "application/json"));
            server.start();

            record ChatRequest(String prompt) {
            }
            record ChatResponse(String reply) {
            }

            ChatResponse response = client.postJson(
                    server.url("/chat").toString(),
                    new ChatRequest("hello"),
                    ChatResponse.class,
                    HttpRequestOptions.jsonPost()
            );

            assertThat(response.reply()).isEqualTo("ok");
            var recorded = server.takeRequest();
            assertThat(recorded.getMethod()).isEqualTo("POST");
            assertThat(recorded.getHeader("Authorization")).isNull();
            assertThat(recorded.getBody().readUtf8()).contains("hello");
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void get_retriesOn503ThenSucceeds() {
        try (var server = new okhttp3.mockwebserver.MockWebServer()) {
            server.enqueue(new okhttp3.mockwebserver.MockResponse().setResponseCode(503));
            server.enqueue(new okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody("ok"));
            server.start();

            AppHttpProperties properties = new AppHttpProperties();
            properties.setMaxRetries(1);
            properties.setRetryBaseDelayMillis(1);
            AppHttpClient retryClient = new AppHttpClient(new OkHttpClient(), properties, new ObjectMapper());

            String body = retryClient.get(server.url("/").toString(), HttpRequestOptions.empty());
            assertThat(body).isEqualTo("ok");
            assertThat(retryClient.getLastAttemptCount()).isEqualTo(2);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void get_throwsHttpClientExceptionOn404() {
        try (var server = new okhttp3.mockwebserver.MockWebServer()) {
            server.enqueue(new okhttp3.mockwebserver.MockResponse().setResponseCode(404).setBody("missing"));
            server.start();

            assertThatThrownBy(() -> client.get(server.url("/missing").toString()))
                    .isInstanceOf(HttpClientException.class)
                    .satisfies(ex -> {
                        HttpClientException httpEx = (HttpClientException) ex;
                        assertThat(httpEx.getHttpStatus()).isEqualTo(404);
                        assertThat(httpEx.getKind()).isEqualTo(HttpClientException.Kind.HTTP_ERROR);
                    });
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
