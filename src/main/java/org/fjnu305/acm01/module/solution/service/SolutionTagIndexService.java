package org.fjnu305.acm01.module.solution.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.solution.entity.SolutionEntity;
import org.fjnu305.acm01.module.solution.mapper.SolutionMapper;
import org.fjnu305.acm01.module.solution.mapper.SolutionTagMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(DataSource.class)
public class SolutionTagIndexService {

    private final SolutionTagMapper solutionTagMapper;
    private final SolutionMapper solutionMapper;

    public static List<String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        List<String> parsed = new ArrayList<>();
        for (String part : tags.split("[,，]")) {
            String tag = part.trim();
            if (!tag.isEmpty() && tag.length() <= 64 && !parsed.contains(tag)) {
                parsed.add(tag);
            }
        }
        return parsed;
    }

    @Transactional
    public void replace(Long solutionId, String tags, boolean published) {
        solutionTagMapper.deleteBySolutionId(solutionId);
        if (!published) {
            return;
        }
        for (String tag : parseTags(tags)) {
            solutionTagMapper.insert(solutionId, tag);
        }
    }

    public void remove(Long solutionId) {
        solutionTagMapper.deleteBySolutionId(solutionId);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureIndex() {
        try {
            solutionTagMapper.createTableIfNeeded();
            if (solutionTagMapper.countAll() > 0) {
                return;
            }
            List<SolutionEntity> rows = solutionMapper.selectPublishedTagRows();
            int n = 0;
            for (SolutionEntity row : rows) {
                replace(row.getId(), row.getTags(), true);
                n++;
            }
            if (n > 0) {
                log.info("Backfilled solution_tag for {} published solutions", n);
            }
        } catch (Exception e) {
            log.warn("solution_tag index init skipped: {}", e.getMessage());
        }
    }
}
