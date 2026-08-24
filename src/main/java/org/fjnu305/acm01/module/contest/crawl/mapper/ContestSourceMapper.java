package org.fjnu305.acm01.module.contest.crawl.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ContestSourceMapper {

    int updateOnSuccess(@Param("sourceCode") String sourceCode);

    int updateOnFailure(@Param("sourceCode") String sourceCode);

    int selectCrawlEnabled(@Param("sourceCode") String sourceCode);

    Integer selectRateLimitSec(@Param("sourceCode") String sourceCode);
}
