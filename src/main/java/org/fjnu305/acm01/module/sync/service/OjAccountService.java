package org.fjnu305.acm01.module.sync.service;

import lombok.RequiredArgsConstructor;
import org.fjnu305.acm01.Common.exception.BusinessException;
import org.fjnu305.acm01.Common.exception.ErrorCode;
import org.fjnu305.acm01.module.sync.dto.BindCfHandleRequest;
import org.fjnu305.acm01.module.sync.entity.CfRatingHistoryEntity;
import org.fjnu305.acm01.module.sync.entity.OjAccountEntity;
import org.fjnu305.acm01.module.sync.entity.UserRatingSnapshotEntity;
import org.fjnu305.acm01.module.sync.mapper.CfRatingHistoryMapper;
import org.fjnu305.acm01.module.sync.mapper.OjAccountMapper;
import org.fjnu305.acm01.module.sync.mapper.UserRatingSnapshotMapper;
import org.fjnu305.acm01.module.sync.vault.CredentialVault;
import org.fjnu305.acm01.module.sync.vo.CfRatingChangeVO;
import org.fjnu305.acm01.module.sync.vo.OjAccountVO;
import org.fjnu305.acm01.module.sync.vo.RatingSnapshotVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OjAccountService {

    private final OjAccountMapper ojAccountMapper;
    private final UserRatingSnapshotMapper snapshotMapper;
    private final CfRatingHistoryMapper historyMapper;
    private final CredentialVault credentialVault;
    private final RatingSyncService ratingSyncService;

    public OjAccountVO bindCfHandle(Long userId, BindCfHandleRequest request) {
        String handle = request.getHandle().trim();
        if (handle.isEmpty()) {
            throw new BusinessException(ErrorCode.OJ_HANDLE_INVALID);
        }

        CredentialVault.EncryptedCredential encrypted = credentialVault.encrypt(
                trimToNull(request.getCredential()));

        OjAccountEntity existing = ojAccountMapper.selectByUserAndPlatform(
                userId, RatingSyncService.PLATFORM_CODEFORCES);

        if (existing != null) {
            existing.setHandle(handle);
            existing.setEncryptedCredential(encrypted.encrypted());
            existing.setCredentialIv(encrypted.iv());
            existing.setStatus(1);
            ojAccountMapper.update(existing);
        } else {
            OjAccountEntity entity = new OjAccountEntity();
            entity.setUserId(userId);
            entity.setPlatform(RatingSyncService.PLATFORM_CODEFORCES);
            entity.setHandle(handle);
            entity.setEncryptedCredential(encrypted.encrypted());
            entity.setCredentialIv(encrypted.iv());
            entity.setStatus(1);
            ojAccountMapper.insert(entity);
            existing = entity;
        }

        ratingSyncService.syncAccount(existing);
        return toVo(existing);
    }

    public List<OjAccountVO> listMyAccounts(Long userId) {
        return ojAccountMapper.selectByUserId(userId).stream()
                .map(this::toVo)
                .toList();
    }

    public List<RatingSnapshotVO> listRatingSnapshots(Long userId) {
        return snapshotMapper.selectRecentByUserAndPlatform(userId, RatingSyncService.PLATFORM_CODEFORCES, 90)
                .stream()
                .map(this::toSnapshotVo)
                .toList();
    }

    public List<CfRatingChangeVO> listCfRatingHistory(Long userId) {
        return historyMapper.selectByUserId(userId).stream()
                .map(this::toHistoryVo)
                .toList();
    }

    private OjAccountVO toVo(OjAccountEntity entity) {
        UserRatingSnapshotEntity latest = snapshotMapper.selectLatestByUserAndPlatform(
                entity.getUserId(), entity.getPlatform());

        return OjAccountVO.builder()
                .id(entity.getId())
                .platform(entity.getPlatform())
                .handle(entity.getHandle())
                .currentRating(latest != null ? latest.getRating() : null)
                .maxRating(latest != null ? latest.getMaxRating() : null)
                .rank(latest != null ? latest.getRank() : null)
                .lastSyncAt(entity.getLastSyncAt())
                .hasCredential(entity.getEncryptedCredential() != null)
                .build();
    }

    private RatingSnapshotVO toSnapshotVo(UserRatingSnapshotEntity entity) {
        return RatingSnapshotVO.builder()
                .platform(entity.getPlatform())
                .rating(entity.getRating())
                .maxRating(entity.getMaxRating())
                .rank(entity.getRank())
                .snapshotDate(entity.getSnapshotDate())
                .build();
    }

    private CfRatingChangeVO toHistoryVo(CfRatingHistoryEntity entity) {
        return CfRatingChangeVO.builder()
                .contestId(entity.getContestId())
                .contestName(entity.getContestName())
                .rank(entity.getRank())
                .oldRating(entity.getOldRating())
                .newRating(entity.getNewRating())
                .ratedAt(entity.getRatedAt())
                .build();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
