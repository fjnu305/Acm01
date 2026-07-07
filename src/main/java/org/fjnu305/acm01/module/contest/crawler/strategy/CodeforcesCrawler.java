package org.fjnu305.acm01.module.contest.crawler.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.Common.enums.ContestSource;
import org.fjnu305.acm01.module.contest.crawler.AbstractContestCrawler;
import org.fjnu305.acm01.module.contest.crawler.dto.CodeforcesContestListResponse;
import org.fjnu305.acm01.module.contest.crawler.dto.CodeforcesContestListResponse.CodeforcesContestItem;
import org.fjnu305.acm01.module.contest.crawler.exception.CrawlFetchException;
import org.fjnu305.acm01.module.contest.crawler.http.CrawlHttpClient;
import org.fjnu305.acm01.module.contest.crawler.http.CrawlHttpProperties;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Codeforces 赛事爬虫策略（迭代 1 首个实现的平台）。
 * <p>
 * <b>数据来源：</b>{@code https://codeforces.com/api/contest.list} 官方 JSON API<br>
 * <b>HTTP：</b>{@link CrawlHttpClient}（OkHttp 连接池 + 最多 3 次重试）<br>
 * <b>流程：</b>请求 API → 过滤有效赛事 → 映射 {@link ContestDTO} → 计算 {@code rawHash}
 * </p>
 * <p>
 * 父类 {@link AbstractContestCrawler#fetchContests()} 负责启用检查、日志与异常兜底；
 * 本类只需实现 {@link #doFetch()}。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CodeforcesCrawler extends AbstractContestCrawler {

    /** Codeforces 官方赛事列表 API（默认不含 gym 赛） */
    private static final String API_URL = "https://codeforces.com/api/contest.list";

    /** 单场赛事详情页 URL 模板 */
    private static final String CONTEST_URL_TEMPLATE = "https://codeforces.com/contest/%d";

    /** 与 application.yml / MySQL 时区保持一致 */
    private static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");

    /** 保留的正式赛事类型：CF Round + Educational Round（API 中 type=ICPC） */
    private static final Set<String> ACCEPTED_TYPES = Set.of("CF", "ICPC");

    /** 已结束赛事最多回溯天数，避免首次爬取写入过多历史数据 */
    private static final int FINISHED_KEEP_DAYS = 90;

    /** 从赛事名称提取 Div.1 ~ Div.4，如 "Codeforces Round #999 (Div. 2)" */
    private static final Pattern DIV_PATTERN =
            Pattern.compile("Div\\.\\s*([1-4])", Pattern.CASE_INSENSITIVE);

    private final CrawlHttpClient crawlHttpClient;
    private final CrawlHttpProperties crawlHttpProperties;

    /**
     * 返回 Codeforces 平台标识，对应 contest.source = {@code codeforces}。
     */
    @Override
    public ContestSource getSource() {
        return ContestSource.CODEFORCES;
    }

    /**
     * 主请求 URL，写入 contest_crawl_log.request_url。
     */
    @Override
    public String getPrimaryRequestUrl() {
        return API_URL;
    }

    /**
     * 拉取 CF API → 过滤 → 映射 ContestDTO（字段对标 contest 表 §3.1）。
     * <p>每条 DTO 设置 rawHash 后由 ContestPersistService 入库。</p>
     */
    @Override
    protected List<ContestDTO> doFetch() {
        CodeforcesContestListResponse response = crawlHttpClient.getJson(API_URL, CodeforcesContestListResponse.class);

        if (response == null || !response.isOk()) {
            String comment = response != null ? response.getComment() : "empty response";
            throw CrawlFetchException.apiError(
                    API_URL,
                    crawlHttpProperties.getMaxRetries(),
                    "Codeforces API 返回失败: " + comment
            );
        }

        List<CodeforcesContestItem> items = response.getResult();
        if (items == null || items.isEmpty()) {
            log.warn("[codeforces] API 返回空列表");
            return List.of();
        }

        LocalDateTime finishedCutoff = LocalDateTime.now(ZONE_SHANGHAI).minusDays(FINISHED_KEEP_DAYS);
        List<ContestDTO> result = new ArrayList<>();

        for (CodeforcesContestItem item : items) {
            ContestDTO dto = toContestDto(item, finishedCutoff);
            if (dto != null) {
                // 供 ContestCrawlService 增量更新：hash 不变则跳过 UPDATE
                dto.setRawHash(computeRawHash(dto));
                result.add(dto);
            }
        }

        log.info("[codeforces] 解析完成：原始 {} 条，有效 {} 条", items.size(), result.size());
        return result;
    }

    /**
     * 单条 CF 记录 → ContestDTO，字段映射 contest 表列。
     *
     * @param finishedCutoff FINISHED 且 start_time 早于此时间的丢弃
     * @return 有效 DTO，过滤掉则 null
     */
    private ContestDTO toContestDto(CodeforcesContestItem item, LocalDateTime finishedCutoff) {
        // 基础字段校验
        if (item.getId() == null || !StringUtils.hasText(item.getName())) {
            return null;
        }
        // 只要 CF / ICPC 正式赛，排除 IOI 等
        if (!StringUtils.hasText(item.getType()) || !ACCEPTED_TYPES.contains(item.getType())) {
            return null;
        }
        // 无有效开始时间的条目（如部分 gym）跳过
        if (item.getStartTimeSeconds() == null || item.getStartTimeSeconds() <= 0) {
            return null;
        }

        LocalDateTime startTime = toLocalDateTime(item.getStartTimeSeconds());
        LocalDateTime endTime = resolveEndTime(startTime, item.getDurationSeconds());

        // 过滤过旧的已结束赛，控制库表体积
        if ("FINISHED".equalsIgnoreCase(item.getPhase()) && startTime.isBefore(finishedCutoff)) {
            return null;
        }

        return ContestDTO.builder()
                .source(ContestSource.CODEFORCES)
                .externalId(String.valueOf(item.getId()))
                .title(item.getName().trim())
                .url(String.format(CONTEST_URL_TEMPLATE, item.getId()))
                .startTime(startTime)
                .endTime(endTime)
                .status(mapPhaseToStatus(item.getPhase(), startTime, endTime))
                .difficulty(extractDifficulty(item.getName()))
                .contestType(item.getType())
                .location("Online")
                .build();
    }

    /** 由 startTime + durationSeconds 计算结束时间；duration 缺失时返回 null */
    private LocalDateTime resolveEndTime(LocalDateTime startTime, Long durationSeconds) {
        if (durationSeconds == null || durationSeconds <= 0) {
            return null;
        }
        return startTime.plusSeconds(durationSeconds);
    }

    /** Unix 秒级时间戳 → 上海时区 LocalDateTime */
    private LocalDateTime toLocalDateTime(long epochSeconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZONE_SHANGHAI);
    }

    /**
     * Codeforces {@code phase} → 统一 {@code status}。
     * <ul>
     *   <li>1 — 即将开始（BEFORE）</li>
     *   <li>2 — 进行中（CODING / 系统测试阶段）</li>
     *   <li>3 — 已结束（FINISHED）</li>
     * </ul>
     * phase 未知时回退到按当前时间与 start/end 推断。
     */
    private Integer mapPhaseToStatus(String phase, LocalDateTime startTime, LocalDateTime endTime) {
        if (StringUtils.hasText(phase)) {
            return switch (phase.toUpperCase()) {
                case "BEFORE" -> 1;
                case "CODING", "PENDING_SYSTEM_TEST", "SYSTEM_TEST" -> 2;
                case "FINISHED" -> 3;
                default -> inferStatusByTime(startTime, endTime);
            };
        }
        return inferStatusByTime(startTime, endTime);
    }

    /** phase 无法映射时，根据当前时间与 start/end 推断 status */
    private Integer inferStatusByTime(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now(ZONE_SHANGHAI);
        if (now.isBefore(startTime)) {
            return 1;
        }
        if (endTime != null && now.isBefore(endTime)) {
            return 2;
        }
        return 3;
    }

    /**
     * 从赛事名称提取难度标签。
     * <ul>
     *   <li>单场：{@code Div. 2} → {@code Div.2}</li>
     *   <li>合并场：{@code Div. 1 + Div. 2} → {@code Div.1+2}</li>
     *   <li>Educational 且无 Div 标记 → {@code Educational}</li>
     * </ul>
     */
    private String extractDifficulty(String name) {
        Matcher matcher = DIV_PATTERN.matcher(name);
        List<Integer> divs = new ArrayList<>();
        while (matcher.find()) {
            int div = Integer.parseInt(matcher.group(1));
            if (divs.isEmpty() || divs.get(divs.size() - 1) != div) {
                divs.add(div);
            }
        }
        if (!divs.isEmpty()) {
            if (divs.size() == 1) {
                return "Div." + divs.get(0);
            }
            StringBuilder sb = new StringBuilder("Div.");
            for (int i = 0; i < divs.size(); i++) {
                if (i > 0) {
                    sb.append('+');
                }
                sb.append(divs.get(i));
            }
            return sb.toString();
        }
        if (name.toLowerCase().contains("educational")) {
            return "Educational";
        }
        return null;
    }
}
