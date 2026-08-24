package org.fjnu305.acm01.support;

import org.fjnu305.acm01.module.user.mapper.RoleMapper;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class SecurityTestMapperConfiguration {

    @Bean
    @Primary
    UserMapper userMapper() {
        return mock(UserMapper.class);
    }

    @Bean
    @Primary
    RoleMapper roleMapper() {
        return mock(RoleMapper.class);
    }
}
