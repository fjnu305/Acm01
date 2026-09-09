package org.fjnu305.acm01.Config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "acm.rate-limit")
public class ApiRateLimitProperties {

    private boolean enabled = true;

    private int windowSeconds = 60;

    /** Per-window cap for anonymous IP. */
    private int anonymousMaxRequests = 120;

    /** Per-window cap for logged-in users. */
    private int authenticatedMaxRequests = 300;
}
