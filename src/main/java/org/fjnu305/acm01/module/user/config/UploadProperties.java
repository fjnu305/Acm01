package org.fjnu305.acm01.module.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "acm.upload")
public class UploadProperties {

    /** Local avatar storage directory (relative to working directory). */
    private String avatarDir = "./data/uploads/avatars";

    /** Public URL prefix; must match WebResourceConfig. */
    private String avatarPublicPath = "/uploads/avatars";

    /** Max avatar file size in bytes (default 2MB). */
    private long avatarMaxBytes = 2 * 1024 * 1024;
}