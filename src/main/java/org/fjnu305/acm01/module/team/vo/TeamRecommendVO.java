package org.fjnu305.acm01.module.team.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TeamRecommendVO {

    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private Integer cfRating;
    private String school;
    private BigDecimal matchScore;
}
