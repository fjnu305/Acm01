package org.fjnu305.acm01.module.search.support;

import org.fjnu305.acm01.module.search.document.SearchDocument;
import org.fjnu305.acm01.module.search.service.SearchSyncService;
import org.fjnu305.acm01.module.solution.entity.SolutionEntity;
import org.fjnu305.acm01.module.team.entity.TeamPostEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SearchDocumentFactory {

    private final SearchSyncService searchSyncService;

    public SearchDocumentFactory(SearchSyncService searchSyncService) {
        this.searchSyncService = searchSyncService;
    }

    public void indexSolution(SolutionEntity solution, String authorName) {
        if (solution == null || solution.getStatus() == null || solution.getStatus() != 1) {
            if (solution != null) {
                searchSyncService.delete("solution", solution.getId());
            }
            return;
        }
        SearchDocument doc = new SearchDocument();
        doc.setId("solution:" + solution.getId());
        doc.setType("solution");
        doc.setRefId(solution.getId());
        doc.setTitle(solution.getTitle());
        doc.setContent(solution.getContent());
        doc.setTags(solution.getTags());
        doc.setAuthorId(solution.getUserId());
        doc.setAuthorName(authorName);
        doc.setCreatedTime(solution.getCreatedTime() != null ? solution.getCreatedTime() : LocalDateTime.now());
        searchSyncService.index(doc);
    }

    public void indexTeamPost(TeamPostEntity post, String authorName) {
        if (post == null || post.getStatus() == null || post.getStatus() != 1) {
            if (post != null) {
                searchSyncService.delete("team", post.getId());
            }
            return;
        }
        SearchDocument doc = new SearchDocument();
        doc.setId("team:" + post.getId());
        doc.setType("team");
        doc.setRefId(post.getId());
        doc.setTitle(post.getTitle());
        doc.setContent(post.getDescription());
        doc.setTags(post.getTags());
        doc.setAuthorId(post.getUserId());
        doc.setAuthorName(authorName);
        doc.setCreatedTime(post.getCreatedTime() != null ? post.getCreatedTime() : LocalDateTime.now());
        searchSyncService.index(doc);
    }

    public void deleteSolution(Long id) {
        searchSyncService.delete("solution", id);
    }

    public void deleteTeamPost(Long id) {
        searchSyncService.delete("team", id);
    }
}
