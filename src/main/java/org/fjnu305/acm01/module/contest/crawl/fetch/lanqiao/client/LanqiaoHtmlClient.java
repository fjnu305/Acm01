package org.fjnu305.acm01.module.contest.crawl.fetch.lanqiao.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawl.common.http.CrawlHttpClient;
import org.fjnu305.acm01.Common.http.AppHttpProperties;
import org.fjnu305.acm01.module.contest.crawl.exception.CrawlFetchException;
import org.fjnu305.acm01.module.contest.crawl.fetch.lanqiao.dto.LanqiaoContestItem;
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
public class LanqiaoHtmlClient {

    public static final String LIST_URL = "https://www.lanqiao.cn/contests/";

    private static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final Pattern ID_PATTERN = Pattern.compile("/contests/(\\d+)");

    private final CrawlHttpClient crawlHttpClient;
    private final AppHttpProperties AppHttpProperties;

    public List<LanqiaoContestItem> fetch() {
        String html = crawlHttpClient.getHtml(LIST_URL, ContestSource.LANQIAO);
        try {
            Document document = Jsoup.parse(html, LIST_URL);
            Elements cards = document.select("a[href*=/contests/]");
            List<LanqiaoContestItem> items = new ArrayList<>();
            for (Element card : cards) {
                LanqiaoContestItem item = parseCard(card);
                if (item != null) {
                    items.add(item);
                }
            }
            log.info("[lanqiao] parsed {} contests", items.size());
            return items;
        } catch (CrawlFetchException e) {
            throw e;
        } catch (Exception e) {
            throw CrawlFetchException.parseError(
                    LIST_URL,
                    AppHttpProperties.getMaxRetries(),
                    crawlHttpClient.getLastAttemptCount(),
                    "Lanqiao HTML parse failed: " + e.getMessage(),
                    e
            );
        }
    }

    private LanqiaoContestItem parseCard(Element card) {
        String href = card.attr("href");
        Matcher matcher = ID_PATTERN.matcher(href);
        if (!matcher.find()) {
            return null;
        }
        String title = card.text().trim();
        if (!StringUtils.hasText(title) || title.length() < 3) {
            Element titleEl = card.selectFirst("h3, h4, .title");
            title = titleEl != null ? titleEl.text().trim() : title;
        }
        if (!StringUtils.hasText(title)) {
            return null;
        }
        LanqiaoContestItem item = new LanqiaoContestItem();
        item.setId(matcher.group(1));
        item.setTitle(title);
        parseTime(card.text(), item);
        if (item.getStartEpochSecond() == null) {
            // default upcoming if no time parsed - skip to avoid bad data
            return null;
        }
        return item;
    }

    private void parseTime(String text, LanqiaoContestItem item) {
        Matcher dateMatcher = Pattern.compile("(\\d{4})[./](\\d{1,2})[./](\\d{1,2})").matcher(text);
        if (dateMatcher.find()) {
            try {
                String dateStr = String.format(Locale.ROOT, "%s-%02d-%02d",
                        dateMatcher.group(1),
                        Integer.parseInt(dateMatcher.group(2)),
                        Integer.parseInt(dateMatcher.group(3)));
                LocalDateTime startTime = LocalDateTime.parse(dateStr + " 09:00",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT));
                item.setStartEpochSecond(startTime.atZone(ZONE_SHANGHAI).toEpochSecond());
                item.setEndEpochSecond(startTime.plusHours(5).atZone(ZONE_SHANGHAI).toEpochSecond());
            } catch (Exception ignored) {
                // no time
            }
        }
    }
}
