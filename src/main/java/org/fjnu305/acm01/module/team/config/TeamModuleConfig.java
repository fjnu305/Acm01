package org.fjnu305.acm01.module.team.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TeamMatchProperties.class)
public class TeamModuleConfig {
}
