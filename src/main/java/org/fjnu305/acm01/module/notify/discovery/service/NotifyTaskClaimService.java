package org.fjnu305.acm01.module.notify.discovery.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.notify.entity.NotifyTaskEntity;
import org.fjnu305.acm01.module.notify.mapper.NotifyTaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotifyTaskClaimService {

    private final NotifyTaskMapper notifyTaskMapper;

    @Transactional
    public int reclaimStale(int staleSeconds) {
        int seconds = Math.max(staleSeconds, 30);
        return notifyTaskMapper.reclaimStaleProcessing(seconds);
    }

    @Transactional
    public List<NotifyTaskEntity> claimDueTasks(int limit) {
        List<NotifyTaskEntity> due = notifyTaskMapper.selectDueTasks(limit);
        if (due.isEmpty()) {
            return List.of();
        }
        List<Long> ids = due.stream().map(NotifyTaskEntity::getId).collect(Collectors.toList());
        notifyTaskMapper.markProcessing(ids);
        for (NotifyTaskEntity task : due) {
            task.setStatus("PROCESSING");
        }
        return due;
    }
}
