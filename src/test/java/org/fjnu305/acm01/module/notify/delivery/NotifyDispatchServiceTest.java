package org.fjnu305.acm01.module.notify.delivery;

import org.fjnu305.acm01.Common.enums.NotifyTaskStatus;
import org.fjnu305.acm01.module.notify.config.NotifyProperties;
import org.fjnu305.acm01.module.notify.delivery.emailhandler.EmailNotifyHandler;
import org.fjnu305.acm01.module.notify.delivery.service.NotifyDispatchService;
import org.fjnu305.acm01.module.notify.delivery.support.NotifyReminderLoader;
import org.fjnu305.acm01.module.notify.delivery.websockethandler.WebSocketNotifyHandler;
import org.fjnu305.acm01.module.notify.entity.NotifyTaskEntity;
import org.fjnu305.acm01.module.notify.mapper.NotifyTaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotifyDispatchServiceTest {

    @Mock
    private NotifyTaskMapper notifyTaskMapper;

    @Mock
    private EmailNotifyHandler emailNotifyHandler;

    @Mock
    private WebSocketNotifyHandler webSocketNotifyHandler;

    @Mock
    private NotifyReminderLoader reminderLoader;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    private NotifyDispatchService dispatchService;

    private final NotifyProperties properties = new NotifyProperties();

    @BeforeEach
    void injectProperties() {
        dispatchService = new NotifyDispatchService(
                notifyTaskMapper,
                emailNotifyHandler,
                webSocketNotifyHandler,
                reminderLoader,
                properties,
                stringRedisTemplate);
    }

    @Test
    void processUserBatch_allChannelsFail_usesBackoff() {
        NotifyTaskEntity task = new NotifyTaskEntity();
        task.setId(1L);
        task.setStatus(NotifyTaskStatus.PROCESSING.getValue());
        task.setRetryCount(0);
        when(notifyTaskMapper.selectById(1L)).thenReturn(task);
        when(reminderLoader.resolveUserId(task)).thenReturn(7L);
        when(webSocketNotifyHandler.sendMerged(anyList())).thenReturn(false);
        when(emailNotifyHandler.sendMerged(anyList())).thenReturn(false);

        dispatchService.processUserBatch(List.of(task));

        verify(notifyTaskMapper).incrementRetryWithBackoff(eq(1L), anyString(), eq(30));
        verify(notifyTaskMapper, never()).markDead(eq(1L), anyString());
    }

    @Test
    void processUserBatch_retriesExhausted_marksDead() {
        NotifyTaskEntity task = new NotifyTaskEntity();
        task.setId(2L);
        task.setStatus(NotifyTaskStatus.PROCESSING.getValue());
        task.setRetryCount(3);
        when(notifyTaskMapper.selectById(2L)).thenReturn(task);
        when(reminderLoader.resolveUserId(task)).thenReturn(7L);
        when(webSocketNotifyHandler.sendMerged(anyList())).thenReturn(false);
        when(emailNotifyHandler.sendMerged(anyList())).thenReturn(false);

        dispatchService.processUserBatch(List.of(task));

        verify(notifyTaskMapper).markDead(eq(2L), anyString());
        verify(notifyTaskMapper, never()).incrementRetryWithBackoff(eq(2L), anyString(), anyInt());
    }

    @Test
    void processUserBatch_websocketSucceeds_skipsEmail() {
        NotifyTaskEntity task = new NotifyTaskEntity();
        task.setId(3L);
        task.setStatus(NotifyTaskStatus.PROCESSING.getValue());
        task.setRetryCount(0);
        when(notifyTaskMapper.selectById(3L)).thenReturn(task);
        when(reminderLoader.resolveUserId(task)).thenReturn(7L);
        when(webSocketNotifyHandler.sendMerged(anyList())).thenReturn(true);

        dispatchService.processUserBatch(List.of(task));

        verify(notifyTaskMapper).markSent(3L);
        verify(emailNotifyHandler, never()).sendMerged(anyList());
    }
}
