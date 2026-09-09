package org.fjnu305.acm01.module.search;

import org.fjnu305.acm01.Common.access.ContentAccessPolicy;
import org.fjnu305.acm01.Common.access.Viewer;
import org.fjnu305.acm01.Security.LoginUser;
import org.fjnu305.acm01.module.search.config.SearchProperties;
import org.fjnu305.acm01.module.search.service.MysqlSearchBackend;
import org.fjnu305.acm01.module.search.service.SearchQueryService;
import org.fjnu305.acm01.module.search.vo.SearchHitVO;
import org.fjnu305.acm01.module.search.vo.SearchResultVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchQueryServiceVisibilityTest {

    @Mock
    private ObjectProvider<org.fjnu305.acm01.module.search.service.ElasticsearchSearchBackend> esProvider;

    @Mock
    private MysqlSearchBackend mysqlSearchBackend;

    @Mock
    private ContentAccessPolicy contentAccessPolicy;

    private SearchQueryService searchQueryService;

    @BeforeEach
    void setUp() {
        SearchProperties properties = new SearchProperties();
        properties.setEnabled(false);
        searchQueryService = new SearchQueryService(properties, esProvider, mysqlSearchBackend, contentAccessPolicy);
    }

    @Test
    void search_dropsHitsPolicyRejects() {
        SearchResultVO raw = new SearchResultVO();
        raw.setHits(List.of(hit("solution", 1L), hit("solution", 2L)));
        raw.setTotal(2);
        when(mysqlSearchBackend.search(eq("dp"), eq("solution"), anyInt(), anyInt(), any()))
                .thenAnswer(invocation -> {
                    SearchResultVO shell = invocation.getArgument(4);
                    shell.setHits(new ArrayList<>(raw.getHits()));
                    shell.setTotal(2);
                    return shell;
                });
        when(contentAccessPolicy.canRead(eq("solution"), eq(1L), any())).thenReturn(true);
        when(contentAccessPolicy.canRead(eq("solution"), eq(2L), any())).thenReturn(false);

        SearchResultVO result = searchQueryService.search(
                "dp", "solution", 1, 20, Viewer.of(new LoginUser(9L, "u", "USER")));

        assertThat(result.getHits()).hasSize(1);
        assertThat(result.getHits().get(0).getRefId()).isEqualTo(1L);
        assertThat(result.getTotal()).isEqualTo(1);
    }

    private static SearchHitVO hit(String type, Long id) {
        SearchHitVO vo = new SearchHitVO();
        vo.setType(type);
        vo.setRefId(id);
        vo.setTitle("t");
        return vo;
    }
}
