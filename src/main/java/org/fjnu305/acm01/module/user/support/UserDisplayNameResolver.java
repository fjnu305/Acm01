package org.fjnu305.acm01.module.user.support;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.user.entity.UserEntity;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.fjnu305.acm01.module.user.service.UserService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDisplayNameResolver {

    private final UserMapper userMapper;

    public String resolve(Long userId) {
        if (userId == null) {
            return "用户";
        }
        UserEntity user = userMapper.selectById(userId);
        return user == null ? "用户" : UserService.displayName(user);
    }

    public String resolve(UserEntity user) {
        return user == null ? "用户" : UserService.displayName(user);
    }
}
