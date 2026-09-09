package org.fjnu305.acm01.module.sync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fjnu305.acm01.module.sync.client.CfRatingClient;
import org.fjnu305.acm01.module.sync.client.dto.CfRatingHistoryResponse.CfRatingChange;
import org.fjnu305.acm01.module.sync.client.dto.CfUserInfoResponse.CfUserInfo;
import org.fjnu305.acm01.module.sync.entity.CfRatingHistoryEntity;
import org.fjnu305.acm01.module.sync.entity.OjAccountEntity;
import org.fjnu305.acm01.module.sync.entity.UserRatingSnapshotEntity;
import org.fjnu305.acm01.module.sync.mapper.CfRatingHistoryMapper;
import org.fjnu305.acm01.module.sync.mapper.OjAccountMapper;
import org.fjnu305.acm01.module.sync.mapper.UserRatingSnapshotMapper;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingSyncService {

    public static final String PLATFORM_CODEFORCES = "codeforces";

    private final OjAccountMapper ojAccountMapper;
    private final UserRatingSnapshotMapper snapshotMapper;
    private final CfRatingHistoryMapper historyMapper;
    private final CfRatingClient cfRatingClient;
    private final UserMapper userMapper;

    @EventListener(ApplicationReadyEvent.class)
    public void ensureHistoryTable() {
        try {
            historyMapper.createTableIfNeeded();
        } catch (Exception e) {
            log.warn("cf_rating_history table init skipped: {}", e.getMessage());
        }
    }

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

        replaceRatingHistory(account);
        ojAccountMapper.updateLastSyncAt(account.getId(), LocalDateTime.now());
    }

    private void replaceRatingHistory(OjAccountEntity account) {
        List<CfRatingChange> changes = cfRatingClient.fetchRatingHistory(account.getHandle());
        if (changes == null) {
            return;
        }
        historyMapper.deleteByUserId(account.getUserId());
        for (CfRatingChange change : changes) {
            if (change.getContestId() == null || change.getNewRating() == null) {
                continue;
            }
            CfRatingHistoryEntity row = new CfRatingHistoryEntity();
            row.setUserId(account.getUserId());
            row.setContestId(change.getContestId());
            row.setContestName(change.getContestName());
            row.setRank(change.getRank());
            row.setOldRating(change.getOldRating() != null ? change.getOldRating() : 0);
            row.setNewRating(change.getNewRating());
            long epoch = change.getRatingUpdateTimeSeconds() != null ? change.getRatingUpdateTimeSeconds() : 0L;
            row.setRatedAt(LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneOffset.UTC));
            historyMapper.insert(row);
        }
        userMapper.updateContestCount(account.getUserId(), changes.size());
    }
}
