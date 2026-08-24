package org.fjnu305.acm01.module.team.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "team.match")
public class TeamMatchProperties {

    private double ratingWeight = 0.4;
    private double regionWeight = 0.3;
    private double tagWeight = 0.3;
    private int recommendLimit = 20;
}
