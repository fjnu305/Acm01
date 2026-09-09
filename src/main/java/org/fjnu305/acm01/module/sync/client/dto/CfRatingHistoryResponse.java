package org.fjnu305.acm01.module.sync.client.dto;

import lombok.Data;

import java.util.List;

@Data
public class CfRatingHistoryResponse {

    private String status;
    private String comment;
    private List<CfRatingChange> result;

    public boolean isOk() {
        return "OK".equals(status);
    }

    @Data
    public static class CfRatingChange {
        private Integer contestId;
        private String contestName;
        private String handle;
        private Integer rank;
        private Long ratingUpdateTimeSeconds;
        private Integer oldRating;
        private Integer newRating;
    }
}
