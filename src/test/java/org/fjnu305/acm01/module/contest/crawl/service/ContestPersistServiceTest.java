package org.fjnu305.acm01.module.contest.crawl.service;

import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawl.mapper.ContestPersistMapper;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.fjnu305.acm01.module.contest.entity.ContestEntity;
import org.fjnu305.acm01.module.contest.event.ContestScheduleChangedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContestPersistServiceTest {

    @Mock
    private ContestPersistMapper contestPersistMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ContestPersistService persistService;

    @Test
    void persistAll_startTimeChange_publishesRescheduleEvent() {
        LocalDateTime oldStart = LocalDateTime.of(2026, 10, 1, 9, 0);
        LocalDateTime newStart = LocalDateTime.of(2026, 10, 1, 12, 0);

        ContestEntity snapshot = new ContestEntity();
        snapshot.setId(88L);
        snapshot.setSource(ContestSource.CODEFORCES.getValue());
        snapshot.setExternalId("123");
        snapshot.setRawHash("old");
        snapshot.setStartTime(oldStart);
        when(contestPersistMapper.selectSnapshotByKeys(anyList())).thenReturn(List.of(snapshot));
        when(contestPersistMapper.batchUpdateBySourceAndExternalId(anyList())).thenReturn(1);

        persistService.persistAll(List.of(ContestDTO.builder()
                .source(ContestSource.CODEFORCES)
                .externalId("123")
                .title("CF 999")
                .startTime(newStart)
                .rawHash("new")
                .build()));

        ArgumentCaptor<ContestScheduleChangedEvent> captor =
                ArgumentCaptor.forClass(ContestScheduleChangedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().contestId()).isEqualTo(88L);
        assertThat(captor.getValue().newStartTime()).isEqualTo(newStart);
        verify(eventPublisher).publishEvent(org.mockito.ArgumentMatchers.any(
                org.fjnu305.acm01.module.contest.event.ContestCatalogChangedEvent.class));
    }
}
