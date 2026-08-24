package org.fjnu305.acm01.module.contest.crawl.fetch.codeforces.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.contest.crawl.fetch.codeforces.dto.CodeforcesContestListResponse;
import org.fjnu305.acm01.module.contest.crawl.fetch.codeforces.dto.CodeforcesContestListResponse.CodeforcesContestItem;
import org.fjnu305.acm01.module.contest.crawl.exception.CrawlFetchException;
import org.fjnu305.acm01.module.contest.crawl.common.http.CrawlHttpClient;
import org.fjnu305.acm01.Common.http.AppHttpProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CodeforcesApiClient {

    public static final String API_URL = "https://codeforces.com/api/contest.list";

    private final CrawlHttpClient crawlHttpClient;
    private final AppHttpProperties appHttpProperties;

    public List<CodeforcesContestItem> fetchContests() {
        CodeforcesContestListResponse response =
                crawlHttpClient.getJson(API_URL, CodeforcesContestListResponse.class);

        if (response == null || !response.isOk()) {
            String comment = response != null ? response.getComment() : "empty response";
            throw CrawlFetchException.apiError(
                    API_URL,
                    appHttpProperties.getMaxRetries(),
                    "Codeforces API è¿”å›žå¤±è´¥: " + comment
            );
        }

        List<CodeforcesContestItem> items = response.getResult();
        if (items == null || items.isEmpty()) {
            log.warn("[codeforces] API 返回空列表");
            return Collections.emptyList();
        }
        return items;
    }
}
