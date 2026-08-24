package org.fjnu305.acm01.module.websocket.api;

import org.fjnu305.acm01.module.websocket.dto.PushMessage;

public interface RealtimePushService {

    void pushToUser(Long userId, PushMessage message);

    boolean isUserOnline(Long userId);
}
