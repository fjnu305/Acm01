package org.fjnu305.acm01.module.contest.crawl.fetch.atcoder;

import org.fjnu305.acm01.module.contest.crawl.fetch.atcoder.client.AtCoderHtmlClient;
import org.fjnu305.acm01.module.contest.crawl.fetch.atcoder.client.AtCoderJsonClient;
import org.fjnu305.acm01.module.contest.crawl.fetch.ContestFetchSupport;
import org.fjnu305.acm01.module.contest.dto.ContestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Comparator;
import java.util.List;

@SpringBootTest
class AtCoderCrawlPreviewTest {

    @Autowired
    private AtCoderJsonClient jsonClient;
    @Autowired
    private AtCoderHtmlClient htmlClient;
    @Autowired
    private AtCoderContestMapper contestMapper;
    @Autowired
    private AtCoderFetchCoordinator fetchCoordinator;
    @Autowired
    private ContestFetchSupport fetchSupport;

    @Test
    void previewCrawlSamples() {
        List<ContestDTO> jsonOnly = contestMapper.mapItems(jsonClient.fetch());
        List<ContestDTO> htmlOnly = contestMapper.mapItems(htmlClient.fetch());
        List<ContestDTO> pipeline = fetchSupport.attachRawHash(fetchCoordinator.fetch());

        System.out.println("\n========== AtCoder 爬虫实时抓取预览 ==========");
        System.out.println("HTML 映射后:     " + htmlOnly.size() + " 条 (主源)");
        System.out.println("JSON 映射后:     " + jsonOnly.size() + " 条 (降级备用)");
        System.out.println("Pipeline 最终:   " + pipeline.size() + " 条 (HTML 主源，失败降级)\n");

        System.out.println("--- 指定样本对比 (HTML 主源 vs JSON 备用) ---");
        for (String id : List.of("abc466", "abc467", "arc225", "adt_easy_20260710_2", "awc0111")) {
            printCompare(id, htmlOnly, jsonOnly);
        }

        System.out.println("\n--- HTML 源样本 (主站列表解析) ---");
        htmlOnly.stream()
                .filter(d -> d.getExternalId() != null && (
                        d.getExternalId().startsWith("abc")
                                || d.getExternalId().startsWith("arc")
                                || d.getExternalId().startsWith("awc")))
                .sorted(Comparator.comparing(ContestDTO::getStartTime).reversed())
                .limit(6)
                .forEach(this::printDto);

        System.out.println("\n--- Pipeline 最终输出 (含 rawHash，最近 8 条) ---");
        pipeline.stream()
                .sorted(Comparator.comparing(ContestDTO::getStartTime).reversed())
                .limit(8)
                .forEach(this::printDto);

        System.out.println("\n--- 有 difficulty 的样本 (HTML 主源自带) ---");
        pipeline.stream()
                .filter(d -> d.getDifficulty() != null && !d.getDifficulty().isBlank())
                .sorted(Comparator.comparing(ContestDTO::getStartTime).reversed())
                .limit(8)
                .forEach(this::printDto);

        long withDifficulty = pipeline.stream()
                .filter(d -> d.getDifficulty() != null && !d.getDifficulty().isBlank())
                .count();
        System.out.println("\n合计 " + withDifficulty + " 条带 difficulty");
    }

    private void printCompare(String id, List<ContestDTO> html, List<ContestDTO> json) {
        ContestDTO h = findById(html, id);
        ContestDTO j = findById(json, id);
        System.out.println("\n[" + id + "]");
        if (h == null) {
            System.out.println("  HTML: (无)");
        } else {
            System.out.println("  HTML → title=" + h.getTitle());
            System.out.println("         start=" + h.getStartTime() + " end=" + h.getEndTime());
            System.out.println("         type=" + h.getContestType() + " diff=" + h.getDifficulty());
        }
        if (j == null) {
            System.out.println("  JSON: (无)");
        } else {
            System.out.println("  JSON → title=" + j.getTitle());
            System.out.println("         start=" + j.getStartTime() + " end=" + j.getEndTime());
            System.out.println("         type=" + j.getContestType() + " diff=" + j.getDifficulty());
        }
    }

    private ContestDTO findById(List<ContestDTO> list, String id) {
        return list.stream()
                .filter(d -> id.equals(d.getExternalId()))
                .findFirst()
                .orElse(null);
    }

    private void printDto(ContestDTO d) {
        System.out.printf(
                "  %-22s | %-55s | %s ~ %s | st=%s type=%-4s diff=%s%n",
                d.getExternalId(),
                truncate(d.getTitle(), 55),
                d.getStartTime(),
                d.getEndTime(),
                d.getStatus(),
                d.getContestType(),
                d.getDifficulty()
        );
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }
}
