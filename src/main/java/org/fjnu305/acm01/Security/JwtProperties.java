package org.fjnu305.acm01.Security;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.fjnu305.acm01.Common.JwtSecretFileUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** 密钥 */
    private String secret;

    /** Token过期时间（毫秒），默认24小时 */
    private Long expiration = 86400000L;

    /** Token前缀 */
    private String tokenPrefix = "Bearer ";

    @PostConstruct
    private void initSecret(){
        secret = JwtSecretFileUtil.getSecretFromFile();
    }
}