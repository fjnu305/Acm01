package org.fjnu305.acm01.module.search.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SearchProperties.class)
public class SearchModuleConfig {
}
