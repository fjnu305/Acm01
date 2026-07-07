package org.fjnu305.acm01.module.contest.crawl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.fjnu305.acm01.module.contest.dto.ContestPersistResult;
import org.fjnu305.acm01.module.contest.entity.ContestEntity;
import org.fjnu305.acm01.module.contest.mapper.ContestMapper;
import org.fjnu305.acm01.module.contest.mapper.ContestMapperSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 赛事持久化：将爬虫输出的 {@link ContestDTO} 批量写入 contest 表。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContestPersistService {

    private final ContestMapper contestMapper;
    private final ContestMapperSupport contestMapperSupport;

    @Transactional
    public ContestPersistResult persistAll(List<ContestDTO> contests) {
        if (contests == null || contests.isEmpty()) {
            return ContestPersistResult.builder().build();
        }

        int ignored = 0;
        Map<String, ContestEntity> incoming = new LinkedHashMap<>();
        for (ContestDTO dto : contests) {
            if (!isValid(dto)) {
                ignored++;
                continue;
            }
            ContestEntity entity = toEntity(dto);
            incoming.put(entity.getSource() + '\0' + entity.getExternalId(), entity);
        }

        if (incoming.isEmpty()) {
            return ContestPersistResult.builder()
                    .fetched(contests.size())
                    .ignored(ignored)
                    .build();
        }

        List<ContestEntity> keys = new ArrayList<>(incoming.size());
        for (ContestEntity entity : incoming.values()) {
            ContestEntity key = new ContestEntity();
            key.setSource(entity.getSource());
            key.setExternalId(entity.getExternalId());
            keys.add(key);
        }

        Map<String, String> existingHash = new HashMap<>();
        for (ContestEntity existing : contestMapper.selectRawHashByKeys(keys)) {
            existingHash.put(existing.getSource() + '\0' + existing.getExternalId(), existing.getRawHash());
        }

        List<ContestEntity> toInsert = new ArrayList<>();
        List<ContestEntity> toUpdate = new ArrayList<>();
        int skipped = 0;
        for (ContestEntity entity : incoming.values()) {
            String businessKey = entity.getSource() + '\0' + entity.getExternalId();
            String oldHash = existingHash.get(businessKey);
            if (oldHash == null) {
                toInsert.add(entity);
            } else if (!Objects.equals(entity.getRawHash(), oldHash)) {
                toUpdate.add(entity);
            } else {
                skipped++;
            }
        }

        int inserted = toInsert.isEmpty() ? 0 : contestMapper.batchInsert(toInsert);
        int updated = toUpdate.isEmpty() ? 0 : contestMapperSupport.batchUpdateBySourceAndExternalId(toUpdate);

        log.info("contest 入库完成：fetched={}, insert={}, update={}, skip={}, ignore={}",
                contests.size(), inserted, updated, skipped, ignored);

        return ContestPersistResult.builder()
                .fetched(contests.size())
                .inserted(inserted)
                .updated(updated)
                .skipped(skipped)
                .ignored(ignored)
                .build();
    }

    private boolean isValid(ContestDTO dto) {
        return dto.getSource() != null
                && StringUtils.hasText(dto.getExternalId())
                && StringUtils.hasText(dto.getTitle())
                && dto.getStartTime() != null;
    }

    private ContestEntity toEntity(ContestDTO dto) {
        ContestEntity entity = new ContestEntity();
        entity.setSource(dto.getSource().getValue());
        entity.setExternalId(dto.getExternalId());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setUrl(dto.getUrl());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setRegisterStart(dto.getRegisterStart());
        entity.setRegisterEnd(dto.getRegisterEnd());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        entity.setDifficulty(dto.getDifficulty());
        entity.setContestType(dto.getContestType());
        entity.setLocation(dto.getLocation());
        entity.setRawHash(dto.getRawHash());
        return entity;
    }
}
