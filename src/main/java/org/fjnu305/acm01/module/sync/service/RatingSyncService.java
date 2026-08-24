package org.fjnu305.acm01.module.sync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.sync.client.CfRatingClient;
import org.fjnu305.acm01.module.sync.client.dto.CfUserInfoResponse.CfUserInfo;
import org.fjnu305.acm01.module.sync.entity.OjAccountEntity;
import org.fjnu305.acm01.module.sync.entity.UserRatingSnapshotEntity;
import org.fjnu305.acm01.module.sync.mapper.OjAccountMapper;
import org.fjnu305.acm01.module.sync.mapper.UserRatingSnapshotMapper;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingSyncService {

    public static final String PLATFORM_CODEFORCES = "codeforces";

    private final OjAccountMapper ojAccountMapper;
    private final UserRatingSnapshotMapper snapshotMapper;
    private final CfRatingClient cfRatingClient;
    private final UserMapper userMapper;

    public void syncAllAccounts() {
        List<OjAccountEntity> accounts = ojAccountMapper.selectAllActive();
        log.info("Rating sync job started, {} active account(s)", accounts.size());

        for (OjAccountEntity account : accounts) {
            try {
                syncAccount(account);
            } catch (Exception e) {
                log.error("Failed to sync account id={} user={} platform={}",
                        account.getId(), account.getUserId(), account.getPlatform(), e);
            }
        }

        log.info("Rating sync job finished");
    }

    public void syncAccount(OjAccountEntity account) {
        if (!PLATFORM_CODEFORCES.equals(account.getPlatform())) {
            return;
        }

        CfUserInfo info = cfRatingClient.fetchUserInfo(account.getHandle());
        if (info == null) {
            return;
        }

        int rating = info.getRating() != null ? info.getRating() : 0;
        int maxRating = info.getMaxRating() != null ? info.getMaxRating() : rating;
        String rank = info.getRank();

        UserRatingSnapshotEntity snapshot = new UserRatingSnapshotEntity();
        snapshot.setUserId(account.getUserId());
        snapshot.setPlatform(PLATFORM_CODEFORCES);
        snapshot.setRating(rating);
        snapshot.setMaxRating(maxRating);
        snapshot.setRank(rank);
        snapshot.setSnapshotDate(LocalDate.now());
        snapshotMapper.upsert(snapshot);

        userMapper.updateCfRating(account.getUserId(), rating);
        userMapper.updateCfHandle(account.getUserId(), account.getHandle());

        ojAccountMapper.updateLastSyncAt(account.getId(), LocalDateTime.now());
    }
}
