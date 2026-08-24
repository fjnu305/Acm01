package org.fjnu305.acm01.module.user.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.Common.result.PageResult;
import org.fjnu305.acm01.module.user.dto.AdminUserVO;
import org.fjnu305.acm01.module.user.dto.UpdateUserRolesRequest;
import org.fjnu305.acm01.module.user.entity.RoleEntity;
import org.fjnu305.acm01.module.user.entity.UserEntity;
import org.fjnu305.acm01.module.user.mapper.RoleMapper;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    public PageResult<AdminUserVO> listUsers(String keyword, int pageNum, int pageSize) {
        int safePageNum = pageNum > 0 ? pageNum : 1;
        int safePageSize = pageSize > 0 ? Math.min(pageSize, MAX_PAGE_SIZE) : 20;
        int offset = (safePageNum - 1) * safePageSize;
        String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;

        long total = userMapper.countPage(kw);
        List<AdminUserVO> list = userMapper.selectPage(kw, offset, safePageSize)
                .stream()
                .map(this::toAdminVO)
                .toList();
        return PageResult.of(list, total, safePageNum, safePageSize);
    }

    public void updateStatus(Long userId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "status must be 0 or 1");
        }
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        userMapper.updateStatus(userId, status);
    }

    @Transactional
    public void updateRoles(Long userId, UpdateUserRolesRequest request) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        roleMapper.deleteUserRoles(userId);
        for (String roleCode : request.getRoles()) {
            RoleEntity role = roleMapper.selectByRoleCode(roleCode.trim());
            if (role == null) {
                throw new BusinessException(ErrorCode.ROLE_NOT_FOUND, "Unknown role: " + roleCode);
            }
            roleMapper.insertUserRole(userId, role.getId());
        }
    }

    private AdminUserVO toAdminVO(UserEntity entity) {
        AdminUserVO vo = new AdminUserVO();
        vo.setId(entity.getId());
        vo.setUsername(entity.getUsername());
        vo.setNickname(entity.getNickname());
        vo.setEmail(entity.getEmail());
        vo.setStatus(entity.getStatus());
        vo.setRoles(roleMapper.selectRoleCodesByUserId(entity.getId()));
        vo.setCreatedTime(entity.getCreatedTime());
        vo.setLastLoginTime(entity.getLastLoginTime());
        return vo;
    }
}
