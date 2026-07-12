package org.fjnu305.acm01.module.contest.crawl.fetch.atcoder.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.contest.crawl.fetch.atcoder.AtCoderCrawlProperties;
import org.fjnu305.acm01.module.contest.crawl.fetch.atcoder.dto.AtCoderContestItem;
import org.fjnu305.acm01.module.contest.crawl.exception.CrawlFetchException;
import org.fjnu305.acm01.module.contest.crawl.common.http.CrawlHttpClient;
import org.fjnu305.acm01.module.contest.crawl.config.CrawlHttpProperties;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class AtCoderHtmlClient {

    private static final ZoneId ZONE_TOKYO = ZoneId.of("Asia/Tokyo");
    private static final DateTimeFormatter HTML_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZ", Locale.ROOT);
    private static final Pattern CONTEST_ID_PATTERN = Pattern.compile("/contests/([^/?#]+)");

    private static final Set<String> TARGET_SECTION_KEYWORDS = Set.of(
            "ongoing contests",
            "upcoming contests",
            "daily contests",
            "recent contests",
            "\u958b\u50ac\u4e88\u5b9a",
            "\u958b\u50ac\u4e2d",
            "\u66dc\u65e5",
            "\u6700\u8fd1"
    );

    private final CrawlHttpClient crawlHttpClient;
    private final CrawlHttpProperties crawlHttpProperties;
    private final AtCoderCrawlProperties atCoderCrawlProperties;

    public List<AtCoderContestItem> fetch() {
        String url = atCoderCrawlProperties.getHtmlUrl();
        String html = crawlHttpClient.getHtml(url);
        try {
            return parseTables(url, html);
        } catch (CrawlFetchException e) {
            throw e;
        } catch (Exception e) {
            throw CrawlFetchException.parseError(
                    url,
                    crawlHttpProperties.getMaxRetries(),
                    crawlHttpClient.getLastAttemptCount(),
                    "AtCoder HTML parse failed: " + e.getMessage(),
                    e
            );
        }
    }

    private List<AtCoderContestItem> parseTables(String url, String html) {
        Document document = Jsoup.parse(html, url);
        List<AtCoderContestItem> result = new ArrayList<>();

        for (Element heading : document.select("h3")) {
            if (!isTargetSection(heading.text())) {
                continue;
            }
            Element table = findNextTable(heading);
            if (table == null) {
                continue;
            }
            parseTableRows(table, result);
        }

        log.info("[atcoder] HTML parse done: {} rows", result.size());
        return result;
    }

    private boolean isTargetSection(String heading) {
        if (!StringUtils.hasText(heading)) {
            return false;
        }
        String lower = heading.toLowerCase(Locale.ROOT);
        return TARGET_SECTION_KEYWORDS.stream().anyMatch(lower::contains);
    }

    private Element findNextTable(Element heading) {
        Element sibling = heading.nextElementSibling();
        while (sibling != null) {
            if ("h3".equalsIgnoreCase(sibling.tagName())) {
                return null;
            }
            Element table = findTableInSubtree(sibling);
            if (table != null) {
                return table;
            }
            sibling = sibling.nextElementSibling();
        }
        return null;
    }

    private Element findTableInSubtree(Element root) {
        if ("table".equalsIgnoreCase(root.tagName())) {
            return root;
        }
        for (Element child : root.children()) {
            Element table = findTableInSubtree(child);
            if (table != null) {
                return table;
            }
        }
        return null;
    }

    private void parseTableRows(Element table, List<AtCoderContestItem> result) {
        Elements rows = table.select("tbody > tr");
        if (rows.isEmpty()) {
            rows = table.select("tr");
        }

        for (Element row : rows) {
            Elements cells = row.select("td");
            if (cells.size() < 4) {
                continue;
            }

            LocalDateTime startTime = parseStartTime(cells.get(0));
            if (startTime == null) {
                continue;
            }

            Element link = cells.get(1).selectFirst("a[href*=/contests/]");
            if (link == null) {
                continue;
            }

            String externalId = extractContestId(link.attr("href"));
            if (!StringUtils.hasText(externalId)) {
                continue;
            }

            String title = link.text();
            Long durationSecond = parseDurationSeconds(cells.get(2).text());
            String ratedRange = cells.get(3).text();

            result.add(toItem(externalId, title, startTime, durationSecond, ratedRange));
        }
    }

    private AtCoderContestItem toItem(String id,
                                       String title,
                                       LocalDateTime startTime,
                                       Long durationSecond,
                                       String ratedRange) {
        AtCoderContestItem item = new AtCoderContestItem();
        item.setId(id);
        item.setTitle(title);
        item.setStartEpochSecond(startTime.atZone(ZONE_TOKYO).toEpochSecond());
        item.setDurationSecond(durationSecond);
        item.setRateChange(ratedRange);
        return item;
    }

    private Long parseDurationSeconds(String durationText) {
        if (!StringUtils.hasText(durationText)) {
            return null;
        }
        String[] parts = durationText.trim().split(":");
        if (parts.length != 2) {
            return null;
        }
        try {
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            return hours * 3600L + minutes * 60L;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDateTime parseStartTime(Element timeCell) {
        if (timeCell == null) {
            return null;
        }
        Element timeElement = timeCell.selectFirst("time.fixtime, time");
        String text = timeElement != null ? timeElement.text() : timeCell.text();
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            ZonedDateTime zoned = ZonedDateTime.parse(text.trim(), HTML_TIME_FORMAT);
            return zoned.withZoneSameInstant(ZONE_TOKYO).toLocalDateTime();
        } catch (DateTimeParseException e) {
            log.debug("[atcoder] cannot parse start time: {}", text);
            return null;
        }
    }


    private String extractContestId(String href) {
        if (!StringUtils.hasText(href)) {
            return null;
        }
        Matcher matcher = CONTEST_ID_PATTERN.matcher(href);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
