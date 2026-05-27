package org.fjnu305.acm01.Common;

import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class JwtSecretFileUtil {
    public static String getSecretFromFile() {
        try {
            ClassPathResource resource = new ClassPathResource("jwt-secret.key");
            InputStream inputStream = resource.getInputStream();
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            throw new RuntimeException("读取JWT密钥文件失败");
        }
    }
}
