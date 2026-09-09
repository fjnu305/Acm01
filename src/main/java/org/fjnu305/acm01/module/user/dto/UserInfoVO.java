package org.fjnu305.acm01.module.user.dto;

import lombok.Builder;
import lombok.Data;
import org.fjnu305.acm01.module.sync.vo.CfRatingChangeVO;
import org.fjnu305.acm01.module.sync.vo.RatingSnapshotVO;

import java.util.List;

@Data
@Builder
public class UserInfoVO {

    private Long userId;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private String school;
    private String bio;
    private String cfHandle;
    private Integer cfRating;
    private Integer solvedCount;
    private Integer acCount;
    private Integer contestCount;
    private List<String> roles;
    private List<RatingSnapshotVO> ratingSnapshots;
    private List<CfRatingChangeVO> cfRatingHistory;
}
