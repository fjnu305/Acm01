package org.fjnu305.acm01.Config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "acm.web")
public class AcmWebProperties {

    /**
     * Browser origins allowed for REST CORS and STOMP. Override with ACM_ALLOWED_ORIGINS.
     */
    private List<String> allowedOrigins = List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173"
    );
}
