package org.fjnu305.acm01.module.contest.crawl.fetch.icpc.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawl.common.http.CrawlHttpClient;
import org.fjnu305.acm01.Common.http.AppHttpProperties;
import org.fjnu305.acm01.module.contest.crawl.exception.CrawlFetchException;
import org.fjnu305.acm01.module.contest.crawl.fetch.icpc.dto.IcpcContestItem;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class IcpcApiClient {

    public static final String API_URL =
            "https://icpc.global/api/community/contests?limit=100&offset=0";

    private final CrawlHttpClient crawlHttpClient;
    private final AppHttpProperties AppHttpProperties;
    private final ObjectMapper objectMapper;

    public List<IcpcContestItem> fetch() {
        String body = crawlHttpClient.getBody(API_URL, ContestSource.ICPC);
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode data = root.isArray() ? root : root.path("data");
            if (!data.isArray()) {
                return List.of();
            }
            List<IcpcContestItem> items = new ArrayList<>();
            for (JsonNode node : data) {
                IcpcContestItem item = parseNode(node);
                if (item != null) {
                    items.add(item);
                }
            }
            log.info("[icpc] parsed {} contests", items.size());
            return items;
        } catch (CrawlFetchException e) {
            throw e;
        } catch (Exception e) {
            throw CrawlFetchException.parseError(
                    API_URL,
                    AppHttpProperties.getMaxRetries(),
                    crawlHttpClient.getLastAttemptCount(),
                    "ICPC API parse failed: " + e.getMessage(),
                    e
            );
        }
    }

    private IcpcContestItem parseNode(JsonNode node) {
        if (node == null || node.isMissingNode()) {
            return null;
        }
        String id = node.has("id") ? node.get("id").asText() : null;
        String name = node.has("name") ? node.get("name").asText(null) : null;
        if (id == null || name == null || name.isBlank()) {
            return null;
        }
        IcpcContestItem item = new IcpcContestItem();
        item.setId(id.trim());
        item.setTitle(name.trim());
        if (node.has("start_time")) {
            item.setStartEpochSecond(parseEpoch(node.get("start_time")));
        }
        if (node.has("end_time")) {
            item.setEndEpochSecond(parseEpoch(node.get("end_time")));
        }
        if (node.has("location")) {
            item.setLocation(node.get("location").asText(null));
        }
        return item;
    }

    private Long parseEpoch(JsonNode node) {
        if (node == null || node.isMissingNode()) {
            return null;
        }
        if (node.isNumber()) {
            long value = node.asLong();
            return value > 1_000_000_000_000L ? value / 1000 : value;
        }
        String text = node.asText(null);
        if (text == null) {
            return null;
        }
        try {
            Instant instant = Instant.parse(text);
            return instant.getEpochSecond();
        } catch (Exception e) {
            return null;
        }
    }
}
