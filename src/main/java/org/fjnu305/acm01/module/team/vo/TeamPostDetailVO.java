package org.fjnu305.acm01.module.team.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeamPostDetailVO extends TeamPostVO {

    private List<TeamMemberVO> members;
}
