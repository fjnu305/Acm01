package org.fjnu305.acm01.module.friend.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.module.friend.config.OfficialUserProperties;
import org.fjnu305.acm01.module.user.entity.UserEntity;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OfficialUserService {

    private final OfficialUserProperties properties;
    private final UserMapper userMapper;

    private volatile Long cachedOfficialUserId;

    public Long requireOfficialUserId() {
        Long id = cachedOfficialUserId;
        if (id != null) {
            return id;
        }
        synchronized (this) {
            if (cachedOfficialUserId != null) {
                return cachedOfficialUserId;
            }
            UserEntity official = userMapper.selectByUsername(properties.getUsername());
            if (official == null || official.getDeleted() != null && official.getDeleted() == 1) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                        "Official user is not configured: " + properties.getUsername());
            }
            cachedOfficialUserId = official.getId();
            return cachedOfficialUserId;
        }
    }

    public boolean isOfficialUser(Long userId) {
        return userId != null && userId.equals(requireOfficialUserId());
    }
}
