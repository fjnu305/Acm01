package org.fjnu305.acm01.module.search.controller;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.result.Result;
import org.fjnu305.acm01.module.search.service.SearchQueryService;
import org.fjnu305.acm01.module.search.vo.SearchResultVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchQueryService searchQueryService;

    @GetMapping
    public Result<SearchResultVO> search(
            @RequestParam("q") String q,
            @RequestParam(required = false, defaultValue = "all") String type,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(searchQueryService.search(q, type, pageNum, pageSize));
    }
}
