package org.fjnu305.acm01.module.sync.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "sync")
public class SyncProperties {

    /** Base64-encoded 32-byte AES-256 key */
    private String credentialKeyBase64 = "dGVzdC1jcmVkZW50aWFsLWtleS0zMi1ieXRlcyE=";

    private Schedule schedule = new Schedule();

    @Data
    public static class Schedule {
        private boolean enabled = true;
        private String cron = "0 0 3 * * ?";
    }
}
