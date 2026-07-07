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
 * 爬虫抽象基类：模板方法固定 fetchContests 流程，子类实现 {@link #doFetch()}。
 * <p>只负责拉取与映射 DTO；启用开关与入库、日志由 {@code contest.crawl} 包负责。</p>
 */
@Slf4j
public abstract class AbstractContestCrawler implements ContestCrawler {

    @Override
    public final List<ContestDTO> fetchContests() {
        log.info("[{}] 开始拉取赛事数据...", getSource().getValue());
        long startMs = System.currentTimeMillis();

        beforeFetch();
        List<ContestDTO> result = doFetch();
        if (result == null) {
            result = Collections.emptyList();
        }
        afterFetch(result.size());
        log.info("[{}] 拉取完成，共 {} 条，耗时 {} ms",
                getSource().getValue(), result.size(), System.currentTimeMillis() - startMs);
        return result;
    }

    protected abstract List<ContestDTO> doFetch();

    protected void beforeFetch() {
    }

    protected void afterFetch(int count) {
    }

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
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }
}
