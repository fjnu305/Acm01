package org.fjnu305.acm01.websocket;

import org.fjnu305.acm01.module.websocket.RedisWsPresenceStore;
import org.fjnu305.acm01.module.websocket.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionManagerClusterPresenceTest {

    @Mock
    private ObjectProvider<RedisWsPresenceStore> presenceStore;
    @Mock
    private RedisWsPresenceStore store;

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        when(presenceStore.getIfAvailable()).thenReturn(store);
        sessionManager = new SessionManager(presenceStore);
    }

    @Test
    void register_writesClusterPresence() {
        sessionManager.register(3L, "sess-a");

        assertThat(sessionManager.hasLocalSession(3L)).isTrue();
        verify(store).addSession(3L, "sess-a");
    }

    @Test
    void isUserOnline_trueWhenOnlyRemoteInstanceHoldsSocket() {
        when(store.isOnline(8L)).thenReturn(true);

        assertThat(sessionManager.hasLocalSession(8L)).isFalse();
        assertThat(sessionManager.isUserOnline(8L)).isTrue();
    }
}
