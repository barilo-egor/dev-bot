package tgb.cryptoexchange.devbot.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import tgb.cryptoexchange.devbot.handler.StartHandler;
import tgb.cryptoexchange.tgcommon.service.RedisUserStateService;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StartFilterTest {

    @Mock
    private RedisUserStateService redisUserStateService;

    @Mock
    private StartHandler startHandler;

    @InjectMocks
    private StartFilter startFilter;

    @Test
    void handleShouldHandleUpdate() {
        Update update = new Update();
        Chat chat = new Chat();
        Message message = new Message();
        Long chatId = 123456789L;
        chat.setId(chatId);
        message.setChat(chat);
        update.setMessage(message);
        startFilter.handle(update);
        verify(redisUserStateService).delete(chatId);
        verify(startHandler).handle(message);
    }

    @Test
    void matchShouldReturnTrueForStartText() {
        Update update = new Update();
        Chat chat = new Chat();
        Message message = new Message();
        Long chatId = 123456789L;
        chat.setId(chatId);
        message.setChat(chat);
        message.setText("/start");
        update.setMessage(message);
        assertTrue(startFilter.match(update));
    }

    @Test
    void matchShouldReturnFalseForCallbackQuery() {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        update.setCallbackQuery(callbackQuery);
        assertFalse(startFilter.match(update));
    }

    @Test
    void matchShouldReturnFalseForPhotoMessage() {
        Update update = new Update();
        Chat chat = new Chat();
        Message message = new Message();
        Long chatId = 123456789L;
        chat.setId(chatId);
        message.setChat(chat);
        message.setPhoto(new ArrayList<>());
        update.setMessage(message);
        assertFalse(startFilter.match(update));
    }

    @Test
    void matchShouldReturnTrueForNotStartText() {
        Update update = new Update();
        Chat chat = new Chat();
        Message message = new Message();
        Long chatId = 123456789L;
        chat.setId(chatId);
        message.setChat(chat);
        message.setText("/help");
        update.setMessage(message);
        assertFalse(startFilter.match(update));
    }
}