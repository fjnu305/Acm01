package org.fjnu305.acm01.module.inbox.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.inbox.dto.BroadcastMessageDTO;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InboxBroadcastService {

    private final InboxDeliveryService inboxDeliveryService;
    private final UserMapper userMapper;

    @Transactional
    public int broadcast(BroadcastMessageDTO request) {
        List<Long> recipients = CollectionUtils.isEmpty(request.getUserIds())
                ? userMapper.selectActiveUserIds()
                : request.getUserIds();

        String title = request.getTitle().trim();
        String body = request.getBody().trim();
        int delivered = 0;
        for (Long recipientId : recipients) {
            if (inboxDeliveryService.deliverBroadcast(recipientId, title, body)) {
                delivered++;
            }
        }
        return delivered;
    }
}
