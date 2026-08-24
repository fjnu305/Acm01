package org.fjnu305.acm01.module.user.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.Security.JwtTokenProvider;
import org.fjnu305.acm01.module.friend.service.FriendshipBootstrapService;
import org.fjnu305.acm01.module.user.dto.AuthResponse;
import org.fjnu305.acm01.module.user.dto.LoginRequest;
import org.fjnu305.acm01.module.user.dto.RegisterRequest;
import org.fjnu305.acm01.module.user.dto.UserInfoVO;
import org.fjnu305.acm01.module.user.entity.RoleEntity;
import org.fjnu305.acm01.module.user.entity.UserEntity;
import org.fjnu305.acm01.module.user.mapper.RoleMapper;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DEFAULT_ROLE_CODE = "USER";

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final FriendshipBootstrapService friendshipBootstrapService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userMapper.countByUsername(request.getUsername()) > 0) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        RoleEntity defaultRole = roleMapper.selectByRoleCode(DEFAULT_ROLE_CODE);
        if (defaultRole == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND,
                    "Default role USER is not configured");
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setStatus(1);
        userMapper.insert(user);

        roleMapper.insertUserRole(user.getId(), defaultRole.getId());
        friendshipBootstrapService.ensureOfficialFriend(user.getId());

        List<String> roles = roleMapper.selectRoleCodesByUserId(user.getId());
        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername(), String.join(",", roles));
        UserInfoVO userInfo = userService.buildUserInfo(user, roles);

        return AuthResponse.builder().token(token).user(userInfo).build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_WRONG);
        }

        userMapper.updateLastLoginTime(user.getId());
        friendshipBootstrapService.ensureOfficialFriend(user.getId());
        List<String> roles = roleMapper.selectRoleCodesByUserId(user.getId());
        if (roles.isEmpty()) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND, "User has no assigned role");
        }

        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername(), String.join(",", roles));
        UserInfoVO userInfo = userService.buildUserInfo(user, roles);

        return AuthResponse.builder().token(token).user(userInfo).build();
    }
}
