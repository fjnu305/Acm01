package org.fjnu305.acm01.module.user.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.user.mapper.RoleMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final RoleMapper roleMapper;

    public Map<String, Object> getDashboardOverview() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", roleMapper.countUsers());
        data.put("pendingReviews", 0);
        data.put("todayRegistrations", 0);
        return data;
    }
}
