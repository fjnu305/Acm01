package org.fjnu305.acm01.module.user.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.user.mapper.RoleMapper;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final RoleMapper roleMapper;
    private final UserMapper userMapper;

    public Map<String, Object> getDashboardOverview() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", roleMapper.countUsers());
        data.put("todayRegistrations", userMapper.countTodayRegistrations());
        data.put("pendingReviews", 0);
        return data;
    }
}
