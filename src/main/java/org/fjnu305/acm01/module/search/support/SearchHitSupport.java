package org.fjnu305.acm01.module.search.support;

import org.fjnu305.acm01.module.search.vo.SearchHitVO;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class SearchHitSupport {

    private SearchHitSupport() {
    }

    public static SearchHitVO toHit(String type, Long refId, String title, String tags,
                                    String authorName, LocalDateTime createdTime, List<String> highlights) {
        SearchHitVO hit = new SearchHitVO();
        hit.setType(type);
        hit.setRefId(refId);
        hit.setTitle(title);
        hit.setSnippet(title);
        hit.setTags(tags);
        hit.setAuthorName(authorName);
        hit.setCreatedTime(createdTime);
        hit.setHighlights(highlights);
        return hit;
    }

    public static List<String> extractHighlights(Map<String, List<String>> highlight) {
        if (highlight == null || highlight.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> merged = new ArrayList<>();
        highlight.values().forEach(merged::addAll);
        return merged;
    }

    public static List<String> highlightText(String text, String keyword) {
        if (!StringUtils.hasText(text) || !StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }
        String lower = text.toLowerCase();
        String key = keyword.toLowerCase();
        int idx = lower.indexOf(key);
        if (idx < 0) {
            return Collections.emptyList();
        }
        String highlighted = text.substring(0, idx)
                + "<em>" + text.substring(idx, idx + keyword.length()) + "</em>"
                + text.substring(idx + keyword.length());
        return List.of(highlighted);
    }

    public static String truncate(String content, int max) {
        if (content == null) {
            return "";
        }
        return content.length() <= max ? content : content.substring(0, max) + "...";
    }

    public static String normalizeType(String type) {
        if (!StringUtils.hasText(type)) {
            return "all";
        }
        String t = type.trim().toLowerCase();
        return switch (t) {
            case "solution", "team", "all" -> t;
            default -> "all";
        };
    }
}
