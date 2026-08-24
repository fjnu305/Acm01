package org.fjnu305.acm01.module.social.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "social.feed")
public class SocialFeedProperties {

    private int cacheTtlMinutes = 5;
    private int hotFeedSize = 50;
}
