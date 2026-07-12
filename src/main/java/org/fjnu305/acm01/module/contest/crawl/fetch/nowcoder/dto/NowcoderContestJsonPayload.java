package org.fjnu305.acm01.module.contest.crawl.fetch.nowcoder.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 牛客列表页 {@code data-json} 属性反序列化结构。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NowcoderContestJsonPayload {

    @JsonProperty("contestId")
    private Long contestId;

    private String contestName;
    private Long contestStartTime;
    private Long contestDuration;
    private Long contestSignUpStartTime;
    private Long contestSignUpEndTime;
    private SettingInfo settingInfo;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SettingInfo {
        private String organizerName;
    }
}
