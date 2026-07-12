package org.fjnu305.acm01.module.contest.crawl.fetch.nowcoder;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawl.fetch.nowcoder.dto.NowcoderContestItem;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class NowcoderContestMapper {

    private static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final String CONTEST_URL_TEMPLATE = "https://ac.nowcoder.com/acm/contest/%s";
    private static final Pattern RATING_COMPARISON_PATTERN =
            Pattern.compile("(?i)Rating([＞>≥＜<≤])(\\d+)");

    private final NowcoderCrawlProperties properties;

    public List<ContestDTO> mapItems(List<NowcoderContestItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        List<ContestDTO> result = new ArrayList<>();
        for (NowcoderContestItem item : items) {
            ContestDTO dto = mapItem(item);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
    }

    public ContestDTO mapItem(NowcoderContestItem item) {
        if (item == null || !StringUtils.hasText(item.getId()) || !StringUtils.hasText(item.getTitle())) {
            return null;
        }

        Long startEpoch = item.getStartEpochSecond();
        if (startEpoch == null || startEpoch <= 0) {
            return null;
        }

        LocalDateTime startTime = toLocalDateTime(startEpoch);
        LocalDateTime endTime = resolveEndTime(startTime, item.getDurationSecond());
        LocalDateTime finishedCutoff = LocalDateTime.now(ZONE_SHANGHAI).minusDays(properties.getFinishedKeepDays());

        if (isFinished(startTime, endTime) && startTime.isBefore(finishedCutoff)) {
            return null;
        }

        return ContestDTO.builder()
                .source(ContestSource.NOWCODER)
                .externalId(item.getId().trim())
                .title(item.getTitle().trim())
                .url(String.format(CONTEST_URL_TEMPLATE, item.getId().trim()))
                .startTime(startTime)
                .endTime(endTime)
                .registerStart(toLocalDateTimeOrNull(item.getRegisterStartEpochSecond()))
                .registerEnd(toLocalDateTimeOrNull(item.getRegisterEndEpochSecond()))
                .status(inferStatus(startTime, endTime))
                .difficulty(normalizeRateChange(item.getRateChange()))
                .contestType(extractContestType(item.getTitle()))
                .location(StringUtils.hasText(item.getOrganizer()) ? item.getOrganizer().trim() : "Online")
                .build();
    }

    private LocalDateTime resolveEndTime(LocalDateTime startTime, Long durationSeconds) {
        if (durationSeconds == null || durationSeconds <= 0) {
            return null;
        }
        return startTime.plusSeconds(durationSeconds);
    }

    private LocalDateTime toLocalDateTime(long epochSeconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZONE_SHANGHAI);
    }

    private LocalDateTime toLocalDateTimeOrNull(Long epochSeconds) {
        if (epochSeconds == null || epochSeconds <= 0) {
            return null;
        }
        return toLocalDateTime(epochSeconds);
    }

    private boolean isFinished(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now(ZONE_SHANGHAI);
        if (endTime != null) {
            return !now.isBefore(endTime);
        }
        return now.isAfter(startTime);
    }

    private Integer inferStatus(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now(ZONE_SHANGHAI);
        if (now.isBefore(startTime)) {
            return 1;
        }
        if (endTime != null && now.isBefore(endTime)) {
            return 2;
        }
        return 3;
    }

    private String extractContestType(String title) {
        if (!StringUtils.hasText(title)) {
            return null;
        }
        if (title.contains("周赛")) {
            return "周赛";
        }
        if (title.contains("月赛")) {
            return "月赛";
        }
        if (title.contains("挑战赛")) {
            return "挑战赛";
        }
        if (title.contains("练习赛")) {
            return "练习赛";
        }
        if (title.contains("多校")) {
            return "多校";
        }
        return null;
    }

    /**
     * 牛客页面展示的是「不计 Rating 的范围」，入库 difficulty 需反转为实际计分范围。
     * 例如不计 Rating＞1599 → difficulty 记为 Rating≤1599。
     */
    private String normalizeRateChange(String excludedRange) {
        if (!StringUtils.hasText(excludedRange)) {
            return null;
        }
        String text = excludedRange.replaceAll("\\s+", "");
        Matcher matcher = RATING_COMPARISON_PATTERN.matcher(text);
        if (!matcher.find()) {
            return text;
        }
        return "Rating" + invertComparator(matcher.group(1)) + matcher.group(2);
    }

    private String invertComparator(String comparator) {
        return switch (comparator) {
            case ">", "＞" -> "≤";
            case "≥" -> "＜";
            case "<", "＜" -> "≥";
            case "≤" -> "＞";
            default -> comparator;
        };
    }
}
