package org.fjnu305.acm01.support;

import org.fjnu305.acm01.Config.SecurityConfig;
import org.fjnu305.acm01.Security.JwtAccessDeniedHandler;
import org.fjnu305.acm01.Security.JwtAuthenticationEntryPoint;
import org.fjnu305.acm01.Security.JwtProperties;
import org.fjnu305.acm01.Security.JwtTokenProvider;
import org.fjnu305.acm01.module.user.mapper.RoleMapper;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/** Test helper. */
@Import({
        SecurityConfig.class,
        TestMethodSecurityConfig.class,
        SecurityTestMapperConfiguration.class,
        JwtTokenProvider.class,
        JwtProperties.class,
        JwtAuthenticationEntryPoint.class,
        JwtAccessDeniedHandler.class
})
public abstract class AbstractSecurityFilterTestSupport {

    @Autowired
    protected UserMapper userMapper;

    @Autowired
    protected RoleMapper roleMapper;
}
