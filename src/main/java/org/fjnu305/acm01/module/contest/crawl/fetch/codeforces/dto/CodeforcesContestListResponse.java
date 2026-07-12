package org.fjnu305.acm01.module.contest.crawl.fetch.codeforces.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Codeforces 官方 API {@code contest.list} 方法的响应体映射?
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodeforcesContestListResponse {

    private String status;
    private String comment;
    private List<CodeforcesContestItem> result;

    public boolean isOk() {
        return "OK".equalsIgnoreCase(status);
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CodeforcesContestItem {

        private Long id;
        private String name;
        private String type;
        private String phase;

        @JsonProperty("durationSeconds")
        private Long durationSeconds;

        @JsonProperty("startTimeSeconds")
        private Long startTimeSeconds;
    }
}
