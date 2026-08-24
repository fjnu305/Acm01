package org.fjnu305.acm01.module.websocket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.websocket.SessionManager;
import org.fjnu305.acm01.module.websocket.api.RealtimePushService;
import org.fjnu305.acm01.module.websocket.dto.PushMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealtimePushServiceImpl implements RealtimePushService {

    public static final String NOTIFY_DESTINATION = "/queue/notifications";

    private final SimpMessagingTemplate messagingTemplate;
    private final SessionManager sessionManager;

    @Override
    public void pushToUser(Long userId, PushMessage message) {
        if (!isUserOnline(userId)) {
            log.debug("User {} offline, skip WebSocket push", userId);
            return;
        }
        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                NOTIFY_DESTINATION,
                message);
        log.debug("WebSocket push sent to user {}: {}", userId, message.getTitle());
    }

    @Override
    public boolean isUserOnline(Long userId) {
        return sessionManager.isUserOnline(userId);
    }
}
