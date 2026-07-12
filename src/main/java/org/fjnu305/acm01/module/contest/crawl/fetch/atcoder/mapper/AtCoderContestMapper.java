package org.fjnu305.acm01.module.contest.crawl.fetch.atcoder;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawl.fetch.atcoder.dto.AtCoderContestItem;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class AtCoderContestMapper {

    private static final ZoneId ZONE_TOKYO = ZoneId.of("Asia/Tokyo");
    private static final String CONTEST_URL_TEMPLATE = "https://atcoder.jp/contests/%s";
    private static final long PERMANENT_DURATION_THRESHOLD = 86400L * 365;

    private final AtCoderCrawlProperties properties;

    public List<ContestDTO> mapItems(List<AtCoderContestItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        List<ContestDTO> result = new ArrayList<>();
        for (AtCoderContestItem item : items) {
            ContestDTO dto = mapItem(item);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
    }

    public ContestDTO mapItem(AtCoderContestItem item) {
        if (item == null || !StringUtils.hasText(item.getId()) || !StringUtils.hasText(item.getTitle())) {
            return null;
        }
        if (isPermanentContest(item)) {
            return null;
        }

        Long startEpoch = item.getStartEpochSecond();
        if (startEpoch == null || startEpoch <= 0) {
            return null;
        }

        LocalDateTime startTime = toLocalDateTime(startEpoch);
        LocalDateTime endTime = resolveEndTime(startTime, item.getDurationSecond());
        LocalDateTime finishedCutoff = LocalDateTime.now(ZONE_TOKYO).minusDays(properties.getFinishedKeepDays());

        if (isFinished(startTime, endTime) && startTime.isBefore(finishedCutoff)) {
            return null;
        }

        return ContestDTO.builder()
                .source(ContestSource.ATCODER)
                .externalId(item.getId().trim())
                .title(cleanTitle(item.getTitle()))
                .url(String.format(CONTEST_URL_TEMPLATE, item.getId().trim()))
                .startTime(startTime)
                .endTime(endTime)
                .status(inferStatus(startTime, endTime))
                .difficulty(normalizeRatedRange(item.getRateChange()))
                .contestType(extractContestType(item.getId(), item.getTitle()))
                .location("Online")
                .build();
    }

    private boolean isPermanentContest(AtCoderContestItem item) {
        String id = item.getId().toLowerCase(Locale.ROOT);
        if (id.contains("practice") || id.startsWith("apg")) {
            return true;
        }
        Long duration = item.getDurationSecond();
        if (duration != null && duration >= PERMANENT_DURATION_THRESHOLD) {
            return true;
        }
        String title = item.getTitle().toLowerCase(Locale.ROOT);
        return title.contains("practice contest") || title.contains("programming guide");
    }

    private LocalDateTime resolveEndTime(LocalDateTime startTime, Long durationSeconds) {
        if (durationSeconds == null || durationSeconds <= 0) {
            return null;
        }
        return startTime.plusSeconds(durationSeconds);
    }

    private LocalDateTime toLocalDateTime(long epochSeconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZONE_TOKYO);
    }

    private boolean isFinished(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now(ZONE_TOKYO);
        if (endTime != null) {
            return now.isAfter(endTime) || now.isEqual(endTime);
        }
        return now.isAfter(startTime);
    }

    private Integer inferStatus(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now(ZONE_TOKYO);
        if (now.isBefore(startTime)) {
            return 1;
        }
        if (endTime != null && now.isBefore(endTime)) {
            return 2;
        }
        return 3;
    }

    private String extractContestType(String id, String title) {
        if (StringUtils.hasText(id)) {
            String lowerId = id.toLowerCase(Locale.ROOT);
            if (lowerId.startsWith("abc")) {
                return "ABC";
            }
            if (lowerId.startsWith("arc")) {
                return "ARC";
            }
            if (lowerId.startsWith("agc")) {
                return "AGC";
            }
            if (lowerId.startsWith("ahc")) {
                return "AHC";
            }
            if (lowerId.startsWith("awtf")) {
                return "AWTF";
            }
        }
        if (!StringUtils.hasText(title)) {
            return null;
        }
        String lower = title.toLowerCase(Locale.ROOT);
        if (lower.contains("beginner contest")) {
            return "ABC";
        }
        if (lower.contains("regular contest")) {
            return "ARC";
        }
        if (lower.contains("grand contest")) {
            return "AGC";
        }
        if (lower.contains("heuristic contest")) {
            return "AHC";
        }
        if (lower.contains("weekday contest")) {
            return "AWC";
        }
        return null;
    }

    private String normalizeRatedRange(String ratedRange) {
        if (!StringUtils.hasText(ratedRange)) {
            return null;
        }
        String trimmed = ratedRange.trim();
        if ("-".equals(trimmed)) {
            return null;
        }
        return trimmed.replaceAll("\\s+", "");
    }

    private String cleanTitle(String title) {
        return title.replaceAll("[ⒶⒽ◉]", "").replaceAll("\\s+", " ").trim();
    }
}
