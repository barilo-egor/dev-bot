package tgb.cryptoexchange.devbot.handler.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import tgb.cryptoexchange.devbot.constants.CallbackQueryId;
import tgb.cryptoexchange.devbot.service.AuthMenuService;
import tgb.cryptoexchange.tgcommon.keyboard.PressedInlineButton;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UpdatePasswordHandlerTest {

    @Mock
    private AuthMenuService authMenuService;

    @InjectMocks
    private UpdatePasswordHandler updatePasswordHandler;

    @ParameterizedTest
    @CsvSource({
            "123456789, 55421",
            "987654321, 244"
    })
    void shouldHandleWithPassedPressedInlineButton(Long chatId, Integer messageId) {
        PressedInlineButton pressedInlineButton = new PressedInlineButton();
        Chat chat = new Chat();
        chat.setId(chatId);
        Message message = new Message();
        message.setMessageId(messageId);
        message.setChat(chat);
        pressedInlineButton.setMessage(message);
        updatePasswordHandler.handle(pressedInlineButton);
        verify(authMenuService).sendUsers(chatId, messageId, CallbackQueryId.UPDATE_PASSWORD_CONFIRM);
    }

    @Test
    void getIdShouldReturnId() {
        assertEquals(CallbackQueryId.UPDATE_PASSWORD.name(), updatePasswordHandler.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {123456789L, 987654321L, 1L})
    void hasAccessShouldReturnTrue(Long chatId) {
        assertTrue(updatePasswordHandler.hasAccess(chatId));
    }
}