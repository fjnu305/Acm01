package org.fjnu305.acm01.module.contest.crawl.fetch.ccpc.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawl.common.http.CrawlHttpClient;
import org.fjnu305.acm01.Common.http.AppHttpProperties;
import org.fjnu305.acm01.module.contest.crawl.exception.CrawlFetchException;
import org.fjnu305.acm01.module.contest.crawl.fetch.ccpc.dto.CcpcContestItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class CcpcHtmlClient {

    public static final String LIST_URL = "https://ccpc.io/contests";

    private static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final Pattern ID_PATTERN = Pattern.compile("/contest/(\\d+)");

    private final CrawlHttpClient crawlHttpClient;
    private final AppHttpProperties AppHttpProperties;

    public List<CcpcContestItem> fetch() {
        String html = crawlHttpClient.getHtml(LIST_URL, ContestSource.CCPC);
        try {
            Document document = Jsoup.parse(html, LIST_URL);
            Elements rows = document.select("table tbody tr");
            List<CcpcContestItem> items = new ArrayList<>();
            for (Element row : rows) {
                CcpcContestItem item = parseRow(row);
                if (item != null) {
                    items.add(item);
                }
            }
            log.info("[ccpc] parsed {} contests", items.size());
            return items;
        } catch (CrawlFetchException e) {
            throw e;
        } catch (Exception e) {
            throw CrawlFetchException.parseError(
                    LIST_URL,
                    AppHttpProperties.getMaxRetries(),
                    crawlHttpClient.getLastAttemptCount(),
                    "CCPC HTML parse failed: " + e.getMessage(),
                    e
            );
        }
    }

    private CcpcContestItem parseRow(Element row) {
        Element link = row.selectFirst("a[href*=/contest/]");
        if (link == null) {
            return null;
        }
        String href = link.attr("href");
        Matcher matcher = ID_PATTERN.matcher(href);
        if (!matcher.find()) {
            return null;
        }
        String title = link.text().trim();
        if (!StringUtils.hasText(title)) {
            return null;
        }
        CcpcContestItem item = new CcpcContestItem();
        item.setId(matcher.group(1));
        item.setTitle(title);
        item.setLocation(extractLocation(row));
        parseTimeRange(row.text(), item);
        if (item.getStartEpochSecond() == null) {
            return null;
        }
        return item;
    }

    private String extractLocation(Element row) {
        Elements cells = row.select("td");
        if (cells.size() >= 3) {
            return cells.get(2).text().trim();
        }
        return "China";
    }

    private void parseTimeRange(String text, CcpcContestItem item) {
        // e.g. 2025-03-15 09:00 - 2025-03-15 14:00
        String[] parts = text.split("\\s+");
        for (int i = 0; i < parts.length - 1; i++) {
            if (parts[i].matches("\\d{4}-\\d{2}-\\d{2}") && parts[i + 1].matches("\\d{2}:\\d{2}")) {
                try {
                    String start = parts[i] + " " + parts[i + 1];
                    LocalDateTime startTime = LocalDateTime.parse(start,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT));
                    item.setStartEpochSecond(startTime.atZone(ZONE_SHANGHAI).toEpochSecond());
                    if (i + 3 < parts.length && parts[i + 2].matches("\\d{4}-\\d{2}-\\d{2}")
                            && parts[i + 3].matches("\\d{2}:\\d{2}")) {
                        String end = parts[i + 2] + " " + parts[i + 3];
                        LocalDateTime endTime = LocalDateTime.parse(end,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT));
                        item.setEndEpochSecond(endTime.atZone(ZONE_SHANGHAI).toEpochSecond());
                    }
                    return;
                } catch (Exception ignored) {
                    // try next match
                }
            }
        }
    }
}
