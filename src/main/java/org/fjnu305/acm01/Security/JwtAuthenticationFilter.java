package org.fjnu305.acm01.Security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.javassist.expr.NewArray;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
       //拿到request头
        try {
            String Token = request.getHeader("Authorization");
            if (StringUtils.hasText(Token) && Token.startsWith("Bearer ")) {
                Token = Token.substring(7);
                String username = jwtTokenProvider.getUsername(Token);
                Long userId = jwtTokenProvider.getUserId(Token);
                String role = jwtTokenProvider.getRoles(Token);
               // List<GrantedAuthority> authorities = NewArray();
                UsernamePasswordAuthenticationToken authentication
                        = new UsernamePasswordAuthenticationToken(username, userId, authorities);
            }
        } catch (Exception e) {
            log.error("认证失败: {}", e.getMessage());

            throw new RuntimeException(e);
        }
        filterChain.doFilter(request,response);
    }
}
