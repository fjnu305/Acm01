package org.fjnu305.acm01.module.sync.client.dto;

import lombok.Data;

import java.util.List;

@Data
public class CfUserInfoResponse {

    private String status;
    private String comment;
    private List<CfUserInfo> result;

    public boolean isOk() {
        return "OK".equals(status);
    }

    @Data
    public static class CfUserInfo {
        private String handle;
        private Integer rating;
        private Integer maxRating;
        private String rank;
    }
}
