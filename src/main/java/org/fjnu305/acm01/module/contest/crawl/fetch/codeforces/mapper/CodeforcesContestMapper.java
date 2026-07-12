package org.fjnu305.acm01.module.contest.crawl.fetch.codeforces;

import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawl.fetch.codeforces.dto.CodeforcesContestListResponse.CodeforcesContestItem;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class CodeforcesContestMapper {

    private static final String CONTEST_URL_TEMPLATE = "https://codeforces.com/contest/%d";
    private static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final Set<String> ACCEPTED_TYPES = Set.of("CF", "ICPC");
    private static final int FINISHED_KEEP_DAYS = 90;
    private static final Pattern DIV_PATTERN =
            Pattern.compile("Div\\.\\s*([1-4])", Pattern.CASE_INSENSITIVE);

    public List<ContestDTO> mapItems(List<CodeforcesContestItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        LocalDateTime finishedCutoff = LocalDateTime.now(ZONE_SHANGHAI).minusDays(FINISHED_KEEP_DAYS);
        List<ContestDTO> result = new ArrayList<>();

        for (CodeforcesContestItem item : items) {
            ContestDTO dto = toContestDto(item, finishedCutoff);
            if (dto != null) {
                result.add(dto);
            }
        }

        log.info("[codeforces] mapped {} of {} items", result.size(), items.size());
        return result;
    }

    private ContestDTO toContestDto(CodeforcesContestItem item, LocalDateTime finishedCutoff) {
        if (item.getId() == null || !StringUtils.hasText(item.getName())) {
            return null;
        }
        if (!StringUtils.hasText(item.getType()) || !ACCEPTED_TYPES.contains(item.getType())) {
            return null;
        }
        if (item.getStartTimeSeconds() == null || item.getStartTimeSeconds() <= 0) {
            return null;
        }

        LocalDateTime startTime = toLocalDateTime(item.getStartTimeSeconds());
        LocalDateTime endTime = resolveEndTime(startTime, item.getDurationSeconds());

        if ("FINISHED".equalsIgnoreCase(item.getPhase()) && startTime.isBefore(finishedCutoff)) {
            return null;
        }

        return ContestDTO.builder()
                .source(ContestSource.CODEFORCES)
                .externalId(String.valueOf(item.getId()))
                .title(item.getName().trim())
                .url(String.format(CONTEST_URL_TEMPLATE, item.getId()))
                .startTime(startTime)
                .endTime(endTime)
                .status(mapPhaseToStatus(item.getPhase(), startTime, endTime))
                .difficulty(extractDifficulty(item.getName()))
                .contestType(item.getType())
                .location("Online")
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

    private Integer mapPhaseToStatus(String phase, LocalDateTime startTime, LocalDateTime endTime) {
        if (StringUtils.hasText(phase)) {
            return switch (phase.toUpperCase()) {
                case "BEFORE" -> 1;
                case "CODING", "PENDING_SYSTEM_TEST", "SYSTEM_TEST" -> 2;
                case "FINISHED" -> 3;
                default -> inferStatusByTime(startTime, endTime);
            };
        }
        return inferStatusByTime(startTime, endTime);
    }

    private Integer inferStatusByTime(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now(ZONE_SHANGHAI);
        if (now.isBefore(startTime)) {
            return 1;
        }
        if (endTime != null && now.isBefore(endTime)) {
            return 2;
        }
        return 3;
    }

    private String extractDifficulty(String name) {
        Matcher matcher = DIV_PATTERN.matcher(name);
        List<Integer> divs = new ArrayList<>();
        while (matcher.find()) {
            int div = Integer.parseInt(matcher.group(1));
            if (divs.isEmpty() || divs.get(divs.size() - 1) != div) {
                divs.add(div);
            }
        }
        if (!divs.isEmpty()) {
            if (divs.size() == 1) {
                return "Div." + divs.get(0);
            }
            StringBuilder sb = new StringBuilder("Div.");
            for (int i = 0; i < divs.size(); i++) {
                if (i > 0) {
                    sb.append('+');
                }
                sb.append(divs.get(i));
            }
            return sb.toString();
        }
        if (name.toLowerCase().contains("educational")) {
            return "Educational";
        }
        return null;
    }
}
