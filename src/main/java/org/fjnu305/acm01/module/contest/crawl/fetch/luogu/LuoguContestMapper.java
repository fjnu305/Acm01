package org.fjnu305.acm01.module.contest.crawl.fetch.luogu;

import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawl.fetch.luogu.dto.LuoguContestItem;
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
public class LuoguContestMapper {

    private static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final String CONTEST_URL_TEMPLATE = "https://www.luogu.com.cn/contest/%s";
    private static final int FINISHED_KEEP_DAYS = 90;

    public List<ContestDTO> mapItems(List<LuoguContestItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        LocalDateTime finishedCutoff = LocalDateTime.now(ZONE_SHANGHAI).minusDays(FINISHED_KEEP_DAYS);
        List<ContestDTO> result = new ArrayList<>();
        for (LuoguContestItem item : items) {
            ContestDTO dto = mapItem(item, finishedCutoff);
            if (dto != null) {
                result.add(dto);
            }
        }
        log.info("[luogu] mapped {} of {} items", result.size(), items.size());
        return result;
    }

    private ContestDTO mapItem(LuoguContestItem item, LocalDateTime finishedCutoff) {
        if (!StringUtils.hasText(item.getId()) || !StringUtils.hasText(item.getName())) {
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
                .source(ContestSource.LUOGU)
                .externalId(item.getId().trim())
                .title(item.getName().trim())
                .url(String.format(CONTEST_URL_TEMPLATE, item.getId().trim()))
                .startTime(startTime)
                .endTime(endTime)
                .status(inferStatus(startTime, endTime))
                .contestType(StringUtils.hasText(item.getType()) ? item.getType().trim() : "\u6d1b\u8c37\u676f")
                .location("Online")
                .build();
    }

    private LocalDateTime toLocalDateTime(long epochSeconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZONE_SHANGHAI);
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
}
