package org.fjnu305.acm01.support;

import org.fjnu305.acm01.Security.JwtTokenProvider;
import org.fjnu305.acm01.module.user.entity.UserEntity;
import org.fjnu305.acm01.module.user.mapper.RoleMapper;
import org.fjnu305.acm01.module.user.mapper.UserMapper;

import java.util.List;

import static org.mockito.Mockito.when;

/** Test helper. */
public final class JwtTestHelper {

    private JwtTestHelper() {
    }

    public static String bearerToken(JwtTokenProvider provider, Long userId, String username, String roles) {
        return "Bearer " + provider.createToken(userId, username, roles);
    }

    public static UserEntity activeUser(Long userId, String username) {
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setUsername(username);
        user.setStatus(1);
        return user;
    }

    public static UserEntity disabledUser(Long userId, String username) {
        UserEntity user = activeUser(userId, username);
        user.setStatus(0);
        return user;
    }

    public static void stubActiveUser(
            UserMapper userMapper,
            RoleMapper roleMapper,
            Long userId,
            String username,
            List<String> roles) {
        when(userMapper.selectById(userId)).thenReturn(activeUser(userId, username));
        when(roleMapper.selectRoleCodesByUserId(userId)).thenReturn(roles);
    }

    public static void stubDisabledUser(UserMapper userMapper, Long userId, String username) {
        when(userMapper.selectById(userId)).thenReturn(disabledUser(userId, username));
    }
}
