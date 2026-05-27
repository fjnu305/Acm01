package org.fjnu305.acm01.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT Token 工具类（精简版）
 * 只包含用户ID、用户名、角色
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    /**
     * 生成Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param roles    角色（多个用逗号分隔）
     * @return JWT Token
     */
    public String createToken(Long userId, String username, String roles) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .setSubject(username)           // 1. 设置主题（用户名）
                .claim("userId", userId)       // 2. 自定义载荷：用户ID
                .claim("roles", roles)         // 3. 自定义载荷：角色列表
                .setIssuedAt(now)              // 4. 设置签发时间
                .setExpiration(expiration)     // 5. 设置过期时间
                .signWith(secretKey, SignatureAlgorithm.HS256) // 6. 签名加密
                .compact();                    // 7. 压缩生成最终 Token
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    /**
     * 从Token中获取角色
     */
    public String getRoles(String token) {
        return parseClaims(token).get("roles", String.class);
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从请求头获取Token
     */
    public String resolveToken(String header) {
        if (header != null && header.startsWith(jwtProperties.getTokenPrefix())) {
            return header.substring(jwtProperties.getTokenPrefix().length());
        }
        return null;
    }

    /**
     * 解析Token
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}