package org.fjnu305.acm01.module.team.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.team.entity.TeamPostEntity;
import org.fjnu305.acm01.module.team.vo.TeamPostDetailVO;
import org.fjnu305.acm01.module.team.vo.TeamPostVO;

import java.util.List;

@Mapper
public interface TeamPostMapper {

    int insert(TeamPostEntity entity);

    TeamPostEntity selectById(@Param("id") Long id);

    TeamPostDetailVO selectDetail(@Param("id") Long id);

    List<TeamPostVO> selectPage(@Param("status") Integer status,
                                @Param("region") String region,
                                @Param("offset") int offset,
                                @Param("limit") int limit);

    long countPage(@Param("status") Integer status, @Param("region") String region);

    List<TeamPostVO> selectMinePage(@Param("userId") Long userId,
                                    @Param("offset") int offset,
                                    @Param("limit") int limit);

    long countMinePage(@Param("userId") Long userId);

    int updateRecruitmentStatus(@Param("id") Long id, @Param("status") int status);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int softDelete(@Param("id") Long id);

    List<TeamPostVO> searchFallback(@Param("keyword") String keyword,
                                    @Param("offset") int offset,
                                    @Param("limit") int limit);

    long countSearchFallback(@Param("keyword") String keyword);
}
