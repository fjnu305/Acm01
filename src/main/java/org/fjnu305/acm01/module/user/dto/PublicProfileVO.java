package org.fjnu305.acm01.module.user.dto;

import lombok.Builder;
import lombok.Data;
import org.fjnu305.acm01.module.sync.vo.CfRatingChangeVO;

import java.util.List;

@Data
@Builder
public class PublicProfileVO {

    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String school;
    private String bio;
    private String cfHandle;
    private Integer cfRating;
    private Integer solvedCount;
    private Boolean isSelf;
    /** NONE | PENDING_SENT | PENDING_RECEIVED | FRIENDS */
    private String friendStatus;
    /** 是否为官方系统好友账号 */
    private Boolean official;
    /** 当前访客是否已关注该用户 */
    private Boolean following;
    private List<CfRatingChangeVO> cfRatingHistory;
}
