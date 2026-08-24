package org.fjnu305.acm01.module.contest.crawl.fetch.luogu.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawl.common.http.CrawlHttpClient;
import org.fjnu305.acm01.Common.http.AppHttpProperties;
import org.fjnu305.acm01.module.contest.crawl.exception.CrawlFetchException;
import org.fjnu305.acm01.module.contest.crawl.fetch.luogu.dto.LuoguContestItem;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LuoguJsonClient {

    public static final String LIST_URL = "https://www.luogu.com.cn/contest/list?_contentOnly=1";

    private final CrawlHttpClient crawlHttpClient;
    private final AppHttpProperties AppHttpProperties;
    private final ObjectMapper objectMapper;

    public List<LuoguContestItem> fetch() {
        String body = crawlHttpClient.getBody(LIST_URL, ContestSource.LUOGU);
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode results = root.path("currentData").path("contests").path("result");
            if (!results.isArray()) {
                return List.of();
            }
            List<LuoguContestItem> items = new ArrayList<>();
            for (JsonNode node : results) {
                LuoguContestItem item = parseNode(node);
                if (item != null) {
                    items.add(item);
                }
            }
            log.info("[luogu] parsed {} contests", items.size());
            return items;
        } catch (CrawlFetchException e) {
            throw e;
        } catch (Exception e) {
            throw CrawlFetchException.parseError(
                    LIST_URL,
                    AppHttpProperties.getMaxRetries(),
                    crawlHttpClient.getLastAttemptCount(),
                    "Luogu JSON parse failed: " + e.getMessage(),
                    e
            );
        }
    }

    private LuoguContestItem parseNode(JsonNode node) {
        if (node == null || node.isMissingNode()) {
            return null;
        }
        String id = node.has("_id") ? node.get("_id").asText() : null;
        String name = node.has("name") ? node.get("name").asText(null) : null;
        if (id == null || name == null || name.isBlank()) {
            return null;
        }
        LuoguContestItem item = new LuoguContestItem();
        item.setId(id.trim());
        item.setName(name.trim());
        if (node.has("startTime")) {
            item.setStartEpochSecond(node.get("startTime").asLong());
        }
        if (node.has("endTime")) {
            item.setEndEpochSecond(node.get("endTime").asLong());
        }
        if (node.has("type")) {
            item.setType(node.get("type").asText(null));
        }
        return item;
    }
}
