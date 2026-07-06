package org.fjnu305.acm01.module.user.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.module.user.dto.UserInfoVO;
import org.fjnu305.acm01.module.user.entity.UserEntity;
import org.fjnu305.acm01.module.user.mapper.RoleMapper;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    public UserInfoVO getUserInfo(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        List<String> roles = roleMapper.selectRoleCodesByUserId(userId);
        return buildUserInfo(user, roles);
    }

    public UserInfoVO buildUserInfo(UserEntity user, List<String> roles) {
        return UserInfoVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .school(user.getSchool())
                .bio(user.getBio())
                .cfRating(user.getCfRating())
                .solvedCount(user.getSolvedCount())
                .acCount(user.getAcCount())
                .contestCount(user.getContestCount())
                .roles(roles)
                .build();
    }
}
