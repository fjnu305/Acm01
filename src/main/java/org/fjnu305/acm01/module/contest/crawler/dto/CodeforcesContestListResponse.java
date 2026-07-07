package org.fjnu305.acm01.module.contest.crawler.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Codeforces 官方 API {@code contest.list} 方法的响应体映射。
 * <p>
 * 仅声明爬虫需要的字段；{@link JsonIgnoreProperties#ignoreUnknown()} 忽略 API 新增字段，增强向前兼容。
 * </p>
 *
 * @see <a href="https://codeforces.com/apiHelp/methods">Codeforces API - contest.list</a>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodeforcesContestListResponse {

    /** API 调用状态，成功时为 {@code OK} */
    private String status;

    /** 失败时的错误说明；成功时通常为 {@code null} */
    private String comment;

    /** 赛事列表；匿名调用时返回全部公开赛事（不含 gym，除非传 gym=true） */
    private List<CodeforcesContestItem> result;

    /** 判断 API 是否成功返回数据 */
    public boolean isOk() {
        return "OK".equalsIgnoreCase(status);
    }

    /**
     * 单条 Codeforces 赛事记录。
     * <p>字段命名与官方 API 一致，见 Codeforces Contest 对象文档。</p>
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CodeforcesContestItem {

        /** 平台侧唯一 ID，对应 {@code contest.external_id} */
        private Long id;

        /** 赛事名称，如 "Codeforces Round #999 (Div. 2)" */
        private String name;

        /**
         * 赛事类型：{@code CF} 正式 Round、{@code ICPC} Educational 等。
         * 爬虫侧通常只保留 CF / ICPC，过滤 IOI 等其他类型。
         */
        private String type;

        /**
         * 赛事阶段：{@code BEFORE} / {@code CODING} / {@code FINISHED} 等，
         * 映射为统一 status（1 即将开始 / 2 进行中 / 3 已结束）。
         */
        private String phase;

        /** 比赛时长（秒），用于计算 endTime */
        @JsonProperty("durationSeconds")
        private Long durationSeconds;

        /** 开始时间 Unix 秒级时间戳；为 0 或缺失的条目应跳过 */
        @JsonProperty("startTimeSeconds")
        private Long startTimeSeconds;
    }
}
