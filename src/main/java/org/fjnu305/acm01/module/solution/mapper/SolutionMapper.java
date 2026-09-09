package org.fjnu305.acm01.module.solution.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.solution.dto.UserTagAggregate;
import org.fjnu305.acm01.module.solution.entity.SolutionEntity;
import org.fjnu305.acm01.module.solution.vo.SolutionDetailVO;
import org.fjnu305.acm01.module.solution.vo.SolutionVO;

import java.util.List;

@Mapper
public interface SolutionMapper {

    int insert(SolutionEntity entity);

    int update(SolutionEntity entity);

    int softDelete(@Param("id") Long id, @Param("userId") Long userId);

    SolutionEntity selectById(@Param("id") Long id);

    SolutionDetailVO selectDetail(@Param("id") Long id, @Param("viewerId") Long viewerId);

    List<SolutionVO> selectPage(@Param("keyword") String keyword,
                                @Param("tag") String tag,
                                @Param("offset") int offset,
                                @Param("limit") int limit);

    List<SolutionVO> selectFavoritePage(@Param("userId") Long userId,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);

    long countFavoritePage(@Param("userId") Long userId);

    long countPage(@Param("keyword") String keyword, @Param("tag") String tag);

    int incrementViewCount(@Param("id") Long id);

    int updateFavoriteCount(@Param("id") Long id, @Param("delta") int delta);

    int takedown(@Param("id") Long id);

    List<SolutionVO> searchFallback(@Param("keyword") String keyword,
                                    @Param("offset") int offset,
                                    @Param("limit") int limit);

    long countSearchFallback(@Param("keyword") String keyword);

    String selectAggregatedTagsByUserId(@Param("userId") Long userId);

    List<UserTagAggregate> selectAggregatedTagsByUserIds(@Param("userIds") List<Long> userIds);

    List<String> selectAllTags();

    List<SolutionEntity> selectPublishedTagRows();
}
