package org.fjnu305.acm01.module.contest.crawl.fetch.icpc;

import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawl.fetch.icpc.dto.IcpcContestItem;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class IcpcContestMapper {

    private static final ZoneId ZONE_UTC = ZoneId.of("UTC");
    private static final String CONTEST_URL_TEMPLATE = "https://icpc.global/community/contest/%s";
    private static final int FINISHED_KEEP_DAYS = 180;

    public List<ContestDTO> mapItems(List<IcpcContestItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        LocalDateTime finishedCutoff = LocalDateTime.now(ZONE_UTC).minusDays(FINISHED_KEEP_DAYS);
        List<ContestDTO> result = new ArrayList<>();
        for (IcpcContestItem item : items) {
            ContestDTO dto = mapItem(item, finishedCutoff);
            if (dto != null) {
                result.add(dto);
            }
        }
        log.info("[icpc] mapped {} of {} items", result.size(), items.size());
        return result;
    }

    private ContestDTO mapItem(IcpcContestItem item, LocalDateTime finishedCutoff) {
        if (!StringUtils.hasText(item.getId()) || !StringUtils.hasText(item.getTitle())) {
            return null;
        }
        Long startEpoch = item.getStartEpochSecond();
        if (startEpoch == null || startEpoch <= 0) {
            return null;
        }
        LocalDateTime startTime = toLocalDateTime(startEpoch);
        LocalDateTime endTime = item.getEndEpochSecond() != null && item.getEndEpochSecond() > 0
                ? toLocalDateTime(item.getEndEpochSecond())
                : null;
        if (isFinished(startTime, endTime) && startTime.isBefore(finishedCutoff)) {
            return null;
        }
        return ContestDTO.builder()
                .source(ContestSource.ICPC)
                .externalId(item.getId().trim())
                .title(item.getTitle().trim())
                .url(String.format(CONTEST_URL_TEMPLATE, item.getId().trim()))
                .startTime(startTime)
                .endTime(endTime)
                .status(inferStatus(startTime, endTime))
                .contestType("ICPC")
                .location(StringUtils.hasText(item.getLocation()) ? item.getLocation().trim() : "Global")
                .build();
    }

    private LocalDateTime toLocalDateTime(long epochSeconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZONE_UTC);
    }

    private boolean isFinished(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now(ZONE_UTC);
        if (endTime != null) {
            return !now.isBefore(endTime);
        }
        return now.isAfter(startTime);
    }

    private Integer inferStatus(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now(ZONE_UTC);
        if (now.isBefore(startTime)) {
            return 1;
        }
        if (endTime != null && now.isBefore(endTime)) {
            return 2;
        }
        return 3;
    }
}
