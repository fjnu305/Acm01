package org.fjnu305.acm01.module.search.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.module.search.support.SearchHitSupport;
import org.fjnu305.acm01.module.search.vo.SearchHitVO;
import org.fjnu305.acm01.module.search.vo.SearchResultVO;
import org.fjnu305.acm01.module.solution.mapper.SolutionMapper;
import org.fjnu305.acm01.module.solution.vo.SolutionVO;
import org.fjnu305.acm01.module.team.mapper.TeamPostMapper;
import org.fjnu305.acm01.module.team.vo.TeamPostVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MysqlSearchBackend implements SearchQueryBackend {

    private final SolutionMapper solutionMapper;
    private final TeamPostMapper teamPostMapper;

    @Override
    public SearchResultVO search(String query, String type, int page, int size, SearchResultVO result) {
        int offset = (page - 1) * size;
        List<SearchHitVO> hits = new ArrayList<>();
        long total;

        if ("all".equals(type)) {
            int fetchLimit = Math.min(offset + size, 200);
            List<SearchHitVO> merged = new ArrayList<>();
            for (SolutionVO s : solutionMapper.searchFallback(query, 0, fetchLimit)) {
                merged.add(SearchHitSupport.toHit("solution", s.getId(), s.getTitle(), s.getTags(),
                        s.getAuthorName(), s.getCreatedTime(),
                        SearchHitSupport.highlightText(s.getTitle(), query)));
            }
            for (TeamPostVO t : teamPostMapper.searchFallback(query, 0, fetchLimit)) {
                merged.add(SearchHitSupport.toHit("team", t.getId(), t.getTitle(), t.getTags(),
                        t.getAuthorName(), t.getCreatedTime(),
                        SearchHitSupport.highlightText(t.getTitle(), query)));
            }
            merged.sort((a, b) -> {
                LocalDateTime ta = a.getCreatedTime();
                LocalDateTime tb = b.getCreatedTime();
                if (ta == null && tb == null) {
                    return 0;
                }
                if (ta == null) {
                    return 1;
                }
                if (tb == null) {
                    return -1;
                }
                return tb.compareTo(ta);
            });
            total = solutionMapper.countSearchFallback(query) + teamPostMapper.countSearchFallback(query);
            int from = Math.min(offset, merged.size());
            int to = Math.min(from + size, merged.size());
            hits = merged.subList(from, to);
        } else if ("solution".equals(type)) {
            total = solutionMapper.countSearchFallback(query);
            for (SolutionVO s : solutionMapper.searchFallback(query, offset, size)) {
                hits.add(SearchHitSupport.toHit("solution", s.getId(), s.getTitle(), s.getTags(),
                        s.getAuthorName(), s.getCreatedTime(),
                        SearchHitSupport.highlightText(s.getTitle(), query)));
            }
        } else {
            total = teamPostMapper.countSearchFallback(query);
            for (TeamPostVO t : teamPostMapper.searchFallback(query, offset, size)) {
                hits.add(SearchHitSupport.toHit("team", t.getId(), t.getTitle(), t.getTags(),
                        t.getAuthorName(), t.getCreatedTime(),
                        SearchHitSupport.highlightText(t.getTitle(), query)));
            }
        }

        result.setHits(hits);
        result.setTotal(total);
        result.setPageNum(page);
        result.setPageSize(size);
        return result;
    }
}
