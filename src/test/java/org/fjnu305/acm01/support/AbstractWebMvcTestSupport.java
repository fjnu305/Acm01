package org.fjnu305.acm01.support;

import org.fjnu305.acm01.Security.JwtAccessDeniedHandler;
import org.fjnu305.acm01.Security.JwtAuthenticationEntryPoint;
import org.fjnu305.acm01.Security.JwtTokenProvider;
import org.fjnu305.acm01.module.user.mapper.RoleMapper;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/** Test helper. */
public abstract class AbstractWebMvcTestSupport {

    @MockitoBean
    protected JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    protected RoleMapper roleMapper;

    @MockitoBean
    protected UserMapper userMapper;

    @MockitoBean
    protected JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    protected JwtAccessDeniedHandler jwtAccessDeniedHandler;
}
