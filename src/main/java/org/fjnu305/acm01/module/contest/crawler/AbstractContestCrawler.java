package org.fjnu305.acm01.module.contest.crawler;

import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

/**
 * 爬虫抽象基类（模板方法模式 + 策略模式）。
 * <p>
 * <b>模板方法：</b>{@link #fetchContests()} 为 {@code final}，固定执行流程：
 * {@code 检查启用 → 爬取前钩子 → 子类 doFetch → 爬取后钩子}，
 * 子类只需实现 {@link #doFetch()}，不用重复写日志与异常包装。
 * </p>
 * <p>
 * <b>公共工具：</b>{@link #computeRawHash(ContestDTO)} 供各平台在构建 DTO 时计算增量哈希。
 * </p>
 */
@Slf4j
public abstract class AbstractContestCrawler implements ContestCrawler {

    /**
     * 模板方法：定义爬虫执行的固定骨架，子类不可重写。
     */
    @Override
    public final List<ContestDTO> fetchContests() {
        // 1. 未启用的爬虫直接跳过，避免无效请求
        if (!enabled()) {
            log.debug("[{}] 爬虫未启用，跳过", getSource().getValue());
            return Collections.emptyList();
        }

        log.info("[{}] 开始拉取赛事数据...", getSource().getValue());
        long startMs = System.currentTimeMillis();

        try {
            beforeFetch();
            List<ContestDTO> result = doFetch();
            // 防御：子类误返回 null 时转为空列表
            if (result == null) {
                result = Collections.emptyList();
            }
            afterFetch(result.size());
            log.info("[{}] 拉取完成，共 {} 条，耗时 {} ms",
                    getSource().getValue(), result.size(), System.currentTimeMillis() - startMs);
            return result;
        } catch (Exception e) {
            log.error("[{}] 拉取失败: {}", getSource().getValue(), e.getMessage(), e);
            onFetchError(e);
            return Collections.emptyList();
        }
    }

    /**
     * 子类实现：具体平台的爬取与解析逻辑。
     * <p>在此方法内完成 HTTP 请求、JSON/HTML 解析，并组装 {@link ContestDTO} 列表。</p>
     *
     * @return 归一化赛事列表
     */
    protected abstract List<ContestDTO> doFetch();

    /**
     * 默认启用。迭代 2 可改为读取 contest_source 表配置。
     */
    @Override
    public boolean enabled() {
        return true;
    }

    /** 爬取前钩子，子类可按需重写（如设置请求头、检查限流） */
    protected void beforeFetch() {
        // 默认空实现
    }

    /** 爬取成功后钩子，子类可按需重写 */
    protected void afterFetch(int count) {
        // 默认空实现
    }

    /** 爬取失败钩子，子类可按需重写（如累加 fail_count） */
    protected void onFetchError(Exception e) {
        // 默认空实现
    }

    /**
     * 根据 DTO 关键字段计算 SHA-256 哈希，写入 rawHash 字段。
     * <p>用于 ContestCrawlService 增量更新：hash 不变则跳过 UPDATE。</p>
     *
     * @param dto 已填充主要字段的 DTO（source、externalId、title、startTime 等）
     * @return 64 位十六进制哈希字符串
     */
    protected String computeRawHash(ContestDTO dto) {
        String raw = String.join("|",
                Objects.toString(dto.getSource(), ""),
                Objects.toString(dto.getExternalId(), ""),
                Objects.toString(dto.getTitle(), ""),
                Objects.toString(dto.getStartTime(), ""),
                Objects.toString(dto.getEndTime(), ""),
                Objects.toString(dto.getUrl(), ""),
                Objects.toString(dto.getDifficulty(), "")
        );
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 在标准 JRE 中必定存在
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }
}
