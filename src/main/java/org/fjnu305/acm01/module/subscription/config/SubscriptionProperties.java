package org.fjnu305.acm01.module.subscription.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "subscription")
public class SubscriptionProperties {

    private List<Integer> allowedRemindMinutes = List.of(1440, 60);
}
