package tgb.cryptoexchange.devbot.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.devbot.config.DevBotConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BannedCacheImplTest {

    @Mock
    private DevBotConfigurationProperties properties;

    @Test
    void shouldCreateWithPassedChatIds() {
        List<Long> chatIds = new ArrayList<>();
        chatIds.add(123456789L);
        chatIds.add(987654321L);
        chatIds.add(555554444L);
        when(properties.getAdminsChatIds()).thenReturn(chatIds);
        BannedCacheImpl bannedCache = new BannedCacheImpl(properties);
        assertAll(
                () -> assertFalse(bannedCache.get(123456789L)),
                () -> assertFalse(bannedCache.get(987654321L)),
                () -> assertTrue(bannedCache.get(123321L))
        );
    }

    @Test
    void shouldCreateWithEmptyAdminChatIds() {
        when(properties.getAdminsChatIds()).thenReturn(null);
        BannedCacheImpl bannedCache = new BannedCacheImpl(properties);
        assertAll(
                () -> assertTrue(bannedCache.get(123456789L)),
                () -> assertTrue(bannedCache.get(987654321L)),
                () -> assertTrue(bannedCache.get(123321L))
        );
    }
}