package org.fjnu305.acm01.module.user.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.module.user.dto.UpdateProfileRequest;
import org.fjnu305.acm01.module.user.dto.UserInfoVO;
import org.fjnu305.acm01.module.user.entity.UserEntity;
import org.fjnu305.acm01.module.sync.service.OjAccountService;
import org.fjnu305.acm01.module.user.mapper.RoleMapper;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final AvatarStorageService avatarStorageService;
    private final OjAccountService ojAccountService;

    public UserInfoVO getUserInfo(Long userId) {
        return buildUserInfo(requireExisting(userId), roleMapper.selectRoleCodesByUserId(userId));
    }

    public UserEntity requireActiveUser(Long userId) {
        UserEntity user = requireExisting(userId);
        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        return user;
    }

    /** 公开资料可见用户（禁用账号对外表现为不存在） */
    public UserEntity requirePublicUser(Long userId) {
        UserEntity user = requireExisting(userId);
        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    public static String displayName(UserEntity user) {
        if (user == null) {
            return "";
        }
        return user.getNickname() != null && !user.getNickname().isBlank()
                ? user.getNickname()
                : user.getUsername();
    }

    private UserEntity requireExisting(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() != null && user.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    public UserInfoVO updateProfile(Long userId, UpdateProfileRequest request) {
        UserEntity user = requireExisting(userId);

        user.setNickname(request.getNickname() != null ? trimToNull(request.getNickname()) : user.getNickname());
        user.setEmail(request.getEmail() != null ? trimToNull(request.getEmail()) : user.getEmail());
        user.setSchool(request.getSchool() != null ? trimToNull(request.getSchool()) : user.getSchool());
        user.setBio(request.getBio() != null ? trimToNull(request.getBio()) : user.getBio());

        int updated = userMapper.updateProfile(user);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return getUserInfo(userId);
    }

    public UserInfoVO uploadAvatar(Long userId, MultipartFile file) {
        UserEntity user = requireExisting(userId);

        String newAvatarUrl = avatarStorageService.storeAvatar(userId, file);
        avatarStorageService.deleteIfLocal(user.getAvatar());

        int updated = userMapper.updateAvatar(userId, newAvatarUrl);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return getUserInfo(userId);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
                .cfHandle(user.getCfHandle())
                .cfRating(user.getCfRating())
                .solvedCount(user.getSolvedCount())
                .acCount(user.getAcCount())
                .contestCount(user.getContestCount())
                .roles(roles)
                .ratingSnapshots(ojAccountService.listRatingSnapshots(user.getId()))
                .cfRatingHistory(ojAccountService.listCfRatingHistory(user.getId()))
                .build();
    }
}
