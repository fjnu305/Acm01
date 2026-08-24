package org.fjnu305.acm01.security;

import jakarta.servlet.ServletException;
import org.fjnu305.acm01.module.user.entity.UserEntity;
import org.fjnu305.acm01.module.user.mapper.RoleMapper;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.fjnu305.acm01.Security.JwtAuthenticationFilter;
import org.fjnu305.acm01.Security.JwtTokenProvider;
import org.fjnu305.acm01.support.JwtTestHelper;
import org.fjnu305.acm01.support.SecurityTestFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validToken_activeUser_setsAuthentication() throws ServletException, java.io.IOException {
        String rawToken = "signed-token";
        when(jwtTokenProvider.resolveToken("Bearer " + rawToken)).thenReturn(rawToken);
        when(jwtTokenProvider.validateToken(rawToken)).thenReturn(true);
        when(jwtTokenProvider.getUserId(rawToken)).thenReturn(SecurityTestFixtures.USER_A_ID);

        UserEntity user = JwtTestHelper.activeUser(
                SecurityTestFixtures.USER_A_ID, SecurityTestFixtures.USER_A_USERNAME);
        when(userMapper.selectById(SecurityTestFixtures.USER_A_ID)).thenReturn(user);
        when(roleMapper.selectRoleCodesByUserId(SecurityTestFixtures.USER_A_ID)).thenReturn(List.of("USER"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + rawToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void validToken_disabledUser_doesNotAuthenticate() throws ServletException, java.io.IOException {
        String rawToken = "signed-token";
        when(jwtTokenProvider.resolveToken("Bearer " + rawToken)).thenReturn(rawToken);
        when(jwtTokenProvider.validateToken(rawToken)).thenReturn(true);
        when(jwtTokenProvider.getUserId(rawToken)).thenReturn(SecurityTestFixtures.USER_A_ID);

        when(userMapper.selectById(SecurityTestFixtures.USER_A_ID))
                .thenReturn(JwtTestHelper.disabledUser(
                        SecurityTestFixtures.USER_A_ID, SecurityTestFixtures.USER_A_USERNAME));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + rawToken);
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void dbRolesOverrideTokenRoles() throws ServletException, java.io.IOException {
        String rawToken = "signed-token";
        when(jwtTokenProvider.resolveToken("Bearer " + rawToken)).thenReturn(rawToken);
        when(jwtTokenProvider.validateToken(rawToken)).thenReturn(true);
        when(jwtTokenProvider.getUserId(rawToken)).thenReturn(SecurityTestFixtures.USER_A_ID);

        when(userMapper.selectById(SecurityTestFixtures.USER_A_ID))
                .thenReturn(JwtTestHelper.activeUser(
                        SecurityTestFixtures.USER_A_ID, SecurityTestFixtures.USER_A_USERNAME));
        when(roleMapper.selectRoleCodesByUserId(SecurityTestFixtures.USER_A_ID)).thenReturn(List.of("ADMIN"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + rawToken);
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void invalidToken_doesNotAuthenticate() throws ServletException, java.io.IOException {
        when(jwtTokenProvider.resolveToken("Bearer bad")).thenReturn("bad");
        when(jwtTokenProvider.validateToken("bad")).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad");
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
