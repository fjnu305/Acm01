package org.fjnu305.acm01.module.team.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fjnu305.acm01.module.team.entity.TeamMemberEntity;
import org.fjnu305.acm01.module.team.vo.TeamMemberVO;
import org.fjnu305.acm01.module.team.vo.TeamMemberPreviewVO;

import java.util.List;

@Mapper
public interface TeamMemberMapper {

    int insert(TeamMemberEntity entity);

    TeamMemberEntity selectById(@Param("id") Long id);

    TeamMemberEntity selectByTeamAndUser(@Param("teamPostId") Long teamPostId, @Param("userId") Long userId);

    List<TeamMemberVO> selectByTeamPostId(@Param("teamPostId") Long teamPostId);

    List<TeamMemberPreviewVO> selectAcceptedPreviewByTeamPostId(@Param("teamPostId") Long teamPostId);

    List<TeamMemberPreviewVO> selectAcceptedPreviewByTeamPostIds(@Param("ids") List<Long> ids);

    List<Long> selectMemberUserIds(@Param("teamPostId") Long teamPostId);

    int countAcceptedByTeamPostId(@Param("teamPostId") Long teamPostId);

    List<TeamMemberEntity> selectPendingByTeamPostId(@Param("teamPostId") Long teamPostId);

    int acceptInvite(@Param("id") Long id, @Param("userId") Long userId);

    int deleteById(@Param("id") Long id);
}
