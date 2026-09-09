package org.fjnu305.acm01.module.sync;

import org.fjnu305.acm01.module.sync.client.CfRatingClient;
import org.fjnu305.acm01.module.sync.client.dto.CfRatingHistoryResponse.CfRatingChange;
import org.fjnu305.acm01.module.sync.client.dto.CfUserInfoResponse.CfUserInfo;
import org.fjnu305.acm01.module.sync.entity.OjAccountEntity;
import org.fjnu305.acm01.module.sync.mapper.CfRatingHistoryMapper;
import org.fjnu305.acm01.module.sync.mapper.OjAccountMapper;
import org.fjnu305.acm01.module.sync.mapper.UserRatingSnapshotMapper;
import org.fjnu305.acm01.module.sync.service.RatingSyncService;
import org.fjnu305.acm01.module.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingSyncServiceTest {

    @Mock
    private OjAccountMapper ojAccountMapper;
    @Mock
    private UserRatingSnapshotMapper snapshotMapper;
    @Mock
    private CfRatingHistoryMapper historyMapper;
    @Mock
    private CfRatingClient cfRatingClient;
    @Mock
    private UserMapper userMapper;

    private RatingSyncService service;

    @BeforeEach
    void setUp() {
        service = new RatingSyncService(
                ojAccountMapper, snapshotMapper, historyMapper, cfRatingClient, userMapper);
    }

    @Test
    void syncAccount_ignoresNonCodeforces() {
        OjAccountEntity account = new OjAccountEntity();
        account.setPlatform("atcoder");

        service.syncAccount(account);

        verify(cfRatingClient, never()).fetchUserInfo(any());
    }

    @Test
    void syncAccount_replacesCfContestHistory() {
        OjAccountEntity account = new OjAccountEntity();
        account.setId(3L);
        account.setUserId(9L);
        account.setHandle("tourist");
        account.setPlatform(RatingSyncService.PLATFORM_CODEFORCES);

        CfUserInfo info = new CfUserInfo();
        info.setRating(3800);
        info.setMaxRating(3979);
        info.setRank("legendary grandmaster");
        when(cfRatingClient.fetchUserInfo("tourist")).thenReturn(info);

        CfRatingChange change = new CfRatingChange();
        change.setContestId(1);
        change.setContestName("CF 1");
        change.setRank(1);
        change.setOldRating(1500);
        change.setNewRating(1600);
        change.setRatingUpdateTimeSeconds(1_267_436_700L);
        when(cfRatingClient.fetchRatingHistory("tourist")).thenReturn(List.of(change));

        service.syncAccount(account);

        verify(historyMapper).deleteByUserId(9L);
        ArgumentCaptor<org.fjnu305.acm01.module.sync.entity.CfRatingHistoryEntity> captor =
                ArgumentCaptor.forClass(org.fjnu305.acm01.module.sync.entity.CfRatingHistoryEntity.class);
        verify(historyMapper).insert(captor.capture());
        assertThat(captor.getValue().getNewRating()).isEqualTo(1600);
        assertThat(captor.getValue().getContestId()).isEqualTo(1);
        verify(userMapper).updateCfRating(9L, 3800);
        verify(userMapper).updateContestCount(eq(9L), eq(1));
    }
}
