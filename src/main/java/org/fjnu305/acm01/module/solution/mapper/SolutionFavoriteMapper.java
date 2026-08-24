package org.fjnu305.acm01.module.solution.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SolutionFavoriteMapper {

    int insert(@Param("userId") Long userId, @Param("solutionId") Long solutionId);

    int delete(@Param("userId") Long userId, @Param("solutionId") Long solutionId);

    int exists(@Param("userId") Long userId, @Param("solutionId") Long solutionId);
}
