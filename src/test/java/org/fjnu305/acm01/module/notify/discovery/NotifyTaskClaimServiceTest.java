package org.fjnu305.acm01.module.notify.discovery;

import org.fjnu305.acm01.module.notify.discovery.service.NotifyTaskClaimService;
import org.fjnu305.acm01.module.notify.entity.NotifyTaskEntity;
import org.fjnu305.acm01.module.notify.mapper.NotifyTaskMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotifyTaskClaimServiceTest {

    @Mock
    private NotifyTaskMapper notifyTaskMapper;

    @InjectMocks
    private NotifyTaskClaimService claimService;

    @Test
    void reclaimStale_enforcesMinimumWindow() {
        when(notifyTaskMapper.reclaimStaleProcessing(30)).thenReturn(2);

        assertThat(claimService.reclaimStale(1)).isEqualTo(2);
        verify(notifyTaskMapper).reclaimStaleProcessing(30);
    }

    @Test
    void claimDueTasks_marksProcessing() {
        NotifyTaskEntity task = new NotifyTaskEntity();
        task.setId(9L);
        when(notifyTaskMapper.selectDueTasks(10)).thenReturn(List.of(task));

        List<NotifyTaskEntity> claimed = claimService.claimDueTasks(10);

        assertThat(claimed).hasSize(1);
        assertThat(claimed.get(0).getStatus()).isEqualTo("PROCESSING");
        verify(notifyTaskMapper).markProcessing(List.of(9L));
    }
}
