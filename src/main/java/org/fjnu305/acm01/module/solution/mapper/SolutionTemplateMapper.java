package org.fjnu305.acm01.module.solution.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.solution.entity.SolutionTemplateEntity;
import org.fjnu305.acm01.module.solution.vo.SolutionTemplateVO;

import java.util.List;

@Mapper
public interface SolutionTemplateMapper {

    List<SolutionTemplateVO> selectAll(@Param("category") String category);

    SolutionTemplateEntity selectById(@Param("id") Long id);
}
