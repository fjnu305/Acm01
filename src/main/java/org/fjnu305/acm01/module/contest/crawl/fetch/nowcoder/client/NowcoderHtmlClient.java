package org.fjnu305.acm01.module.contest.crawl.fetch.nowcoder.client;

import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.contest.crawl.exception.CrawlFetchException;
import org.fjnu305.acm01.module.contest.crawl.common.http.CrawlHttpClient;
import org.fjnu305.acm01.module.contest.crawl.config.CrawlHttpProperties;
import org.fjnu305.acm01.module.contest.crawl.fetch.nowcoder.NowcoderCrawlProperties;
import org.fjnu305.acm01.module.contest.crawl.fetch.nowcoder.dto.NowcoderContestItem;
import org.fjnu305.acm01.module.contest.crawl.fetch.nowcoder.dto.NowcoderContestJsonPayload;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NowcoderHtmlClient {

    private final CrawlHttpClient crawlHttpClient;
    private final CrawlHttpProperties crawlHttpProperties;
    private final NowcoderCrawlProperties nowcoderCrawlProperties;
    private final ObjectMapper objectMapper;

    public List<NowcoderContestItem> fetch() {
        String url = nowcoderCrawlProperties.getListUrl();
        String html = crawlHttpClient.getHtml(url);
        try {
            return parseContests(url, html);
        } catch (CrawlFetchException e) {
            throw e;
        } catch (Exception e) {
            throw CrawlFetchException.parseError(
                    url,
                    crawlHttpProperties.getMaxRetries(),
                    crawlHttpClient.getLastAttemptCount(),
                    "Nowcoder HTML parse failed: " + e.getMessage(),
                    e
            );
        }
    }

    private List<NowcoderContestItem> parseContests(String url, String html) {
        Document document = Jsoup.parse(html, url);
        Elements items = document.select("div.platform-item.js-item[data-id]");
        List<NowcoderContestItem> result = new ArrayList<>();

        for (Element element : items) {
            NowcoderContestItem item = parseItem(element);
            if (item != null) {
                result.add(item);
            }
        }

        log.info("[nowcoder] HTML parse done: {} rows", result.size());
        return result;
    }

    private NowcoderContestItem parseItem(Element element) {
        String dataId = element.attr("data-id");
        if (!StringUtils.hasText(dataId)) {
            return null;
        }

        NowcoderContestJsonPayload payload = parsePayload(element.attr("data-json"));
        if (payload == null || payload.getContestStartTime() == null || payload.getContestStartTime() <= 0) {
            return null;
        }

        NowcoderContestItem item = new NowcoderContestItem();
        item.setId(payload.getContestId() != null ? String.valueOf(payload.getContestId()) : dataId.trim());
        item.setTitle(StringUtils.hasText(payload.getContestName())
                ? payload.getContestName().trim()
                : extractTitle(element));
        item.setStartEpochSecond(payload.getContestStartTime() / 1000);
        if (payload.getContestDuration() != null && payload.getContestDuration() > 0) {
            item.setDurationSecond(payload.getContestDuration() / 1000);
        }
        if (payload.getContestSignUpStartTime() != null && payload.getContestSignUpStartTime() > 0) {
            item.setRegisterStartEpochSecond(payload.getContestSignUpStartTime() / 1000);
        }
        if (payload.getContestSignUpEndTime() != null && payload.getContestSignUpEndTime() > 0) {
            item.setRegisterEndEpochSecond(payload.getContestSignUpEndTime() / 1000);
        }
        if (payload.getSettingInfo() != null && StringUtils.hasText(payload.getSettingInfo().getOrganizerName())) {
            item.setOrganizer(payload.getSettingInfo().getOrganizerName().trim());
        } else {
            item.setOrganizer(extractOrganizer(element));
        }
        item.setRateChange(extractRateChange(element));
        return item;
    }

    private NowcoderContestJsonPayload parsePayload(String rawJson) {
        if (!StringUtils.hasText(rawJson)) {
            return null;
        }
        String json = unescapeHtmlJson(rawJson);
        try {
            return objectMapper.readValue(json, NowcoderContestJsonPayload.class);
        } catch (Exception e) {
            log.debug("[nowcoder] cannot parse data-json: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 牛客列表页 data-json 在 HTML 属性里可能被双重编码为 {@code &amp;quot;}，
     * Jsoup 读属性后仍是 {@code &quot;} 字面量，需再反转义成合法 JSON。
     */
    private String unescapeHtmlJson(String rawJson) {
        String decoded = rawJson;
        for (int i = 0; i < 3; i++) {
            String next = Parser.unescapeEntities(decoded, false);
            if (next.equals(decoded)) {
                break;
            }
            decoded = next;
        }
        return decoded;
    }

    private String extractTitle(Element element) {
        Element titleLink = element.selectFirst("h4 a[href*=/acm/contest/]");
        return titleLink != null ? titleLink.text().trim() : null;
    }

    private String extractOrganizer(Element element) {
        for (Element li : element.select("ul.platform-info > li")) {
            String text = li.text();
            if (text.startsWith("主办方：")) {
                return text.substring("主办方：".length()).trim();
            }
        }
        return null;
    }

    private String extractRateChange(Element element) {
        Element ratingLi = element.selectFirst("li.icon-nc-flash2");
        if (ratingLi == null) {
            return null;
        }
        String text = ratingLi.text().trim();
        int colon = text.indexOf('：');
        if (colon >= 0 && colon < text.length() - 1) {
            return text.substring(colon + 1).trim();
        }
        return text;
    }
}
