package org.fjnu305.acm01.Config;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.user.config.UploadProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
@RequiredArgsConstructor
public class WebResourceConfig implements WebMvcConfigurer {

    private final UploadProperties uploadProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path avatarDir = Path.of(uploadProperties.getAvatarDir()).toAbsolutePath().normalize();
        String location = avatarDir.toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }

        String pattern = uploadProperties.getAvatarPublicPath();
        if (!pattern.endsWith("/")) {
            pattern += "/";
        }
        pattern += "**";

        registry.addResourceHandler(pattern)
                .addResourceLocations(location);
    }
}
