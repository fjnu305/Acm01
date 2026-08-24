package org.fjnu305.acm01.module.search.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "search")
public class SearchProperties {

        /** Whether Elasticsearch search is enabled. */
    private boolean enabled = false;

    private String indexName = "acm_search";

    private String elasticsearchUri = "http://localhost:9200";

    private int defaultPageSize = 20;
}
