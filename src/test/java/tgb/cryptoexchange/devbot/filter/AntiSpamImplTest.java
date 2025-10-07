package tgb.cryptoexchange.devbot.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AntiSpamImplTest {

    private AntiSpamImpl antiSpamService;

    @BeforeEach
    void setUp() {
        antiSpamService = new AntiSpamImpl(3); // messageLimit = 3
    }

    @Test
    void testNoSpamInitially() {
        Long chatId = 1L;
        boolean result = antiSpamService.isSpam(chatId);
        assertFalse(result, "Первое сообщение не должно считаться спамом");
    }

    @Test
    void testSpamWhenLimitExceeded() {
        Long chatId = 1L;

        // 1, 2, 3 — не спам
        assertFalse(antiSpamService.isSpam(chatId));
        assertFalse(antiSpamService.isSpam(chatId));
        assertFalse(antiSpamService.isSpam(chatId));

        // 4-е сообщение — должно вызвать ветку (activity.getMessageCount() >= limit)
        boolean result = antiSpamService.isSpam(chatId);

        assertTrue(result, "Четвёртое сообщение подряд должно считаться спамом");
    }

    @Test
    void testDifferentUsersAreIndependent() {
        Long user1 = 1L;
        Long user2 = 2L;

        antiSpamService.isSpam(user1);
        antiSpamService.isSpam(user1);
        antiSpamService.isSpam(user1);
        boolean spam1 = antiSpamService.isSpam(user1);
        assertTrue(spam1);

        boolean spam2 = antiSpamService.isSpam(user2);
        assertFalse(spam2, "Другой пользователь не должен считаться спамером");
    }

    @Test
    void testOldMessagesAreCleaned() {
        AntiSpamImpl.UserUpdateActivity activity = new AntiSpamImpl.UserUpdateActivity();
        long baseTime = System.currentTimeMillis();

        activity.addMessage(baseTime - 15_000); // старое сообщение
        activity.addMessage(baseTime - 5_000);  // свежее сообщение
        activity.cleanOldMessages(baseTime, 10_000);

        assertEquals(1, activity.getMessageCount(),
                "После очистки должно остаться только одно свежее сообщение");
    }

    @Test
    void testCleanOldMessagesManually() {
        AntiSpamImpl.UserUpdateActivity activity = new AntiSpamImpl.UserUpdateActivity();
        long now = System.currentTimeMillis();

        activity.addMessage(now - 15_000);
        activity.addMessage(now - 5_000);
        activity.cleanOldMessages(now, 10_000);

        assertEquals(1, activity.getMessageCount(), "Должно остаться только одно (свежее) сообщение");
    }
}
