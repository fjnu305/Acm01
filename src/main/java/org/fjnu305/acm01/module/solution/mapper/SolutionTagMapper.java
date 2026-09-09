package org.fjnu305.acm01.module.solution.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.solution.vo.SolutionTagVO;

import java.util.List;

@Mapper
public interface SolutionTagMapper {

    int createTableIfNeeded();

    int deleteBySolutionId(@Param("solutionId") Long solutionId);

    int insert(@Param("solutionId") Long solutionId, @Param("tag") String tag);

    int countAll();

    List<SolutionTagVO> selectCounts();
}
