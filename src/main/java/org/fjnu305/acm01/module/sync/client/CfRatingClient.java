package org.fjnu305.acm01.module.sync.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.contest.crawl.common.http.CrawlHttpClient;
import org.fjnu305.acm01.module.sync.client.dto.CfUserInfoResponse;
import org.fjnu305.acm01.module.sync.client.dto.CfUserInfoResponse.CfUserInfo;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CfRatingClient {

    private static final String API_BASE = "https://codeforces.com/api/user.info?handles=";

    private final CrawlHttpClient crawlHttpClient;

    public CfUserInfo fetchUserInfo(String handle) {
        String encoded = URLEncoder.encode(handle, StandardCharsets.UTF_8);
        String url = API_BASE + encoded;

        CfUserInfoResponse response = crawlHttpClient.getJson(url, CfUserInfoResponse.class);
        if (response == null || !response.isOk()) {
            String comment = response != null ? response.getComment() : "empty response";
            log.warn("Codeforces user.info failed for {}: {}", handle, comment);
            return null;
        }

        List<CfUserInfo> items = response.getResult();
        if (items == null || items.isEmpty()) {
            return null;
        }
        return items.get(0);
    }

    public List<CfUserInfo> fetchUserInfoBatch(List<String> handles) {
        if (handles == null || handles.isEmpty()) {
            return Collections.emptyList();
        }
        String joined = String.join(";", handles);
        String encoded = URLEncoder.encode(joined, StandardCharsets.UTF_8);
        String url = API_BASE + encoded;

        CfUserInfoResponse response = crawlHttpClient.getJson(url, CfUserInfoResponse.class);
        if (response == null || !response.isOk()) {
            return Collections.emptyList();
        }
        List<CfUserInfo> items = response.getResult();
        return items != null ? items : Collections.emptyList();
    }
}
