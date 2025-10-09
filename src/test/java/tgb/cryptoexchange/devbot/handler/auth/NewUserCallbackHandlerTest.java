package tgb.cryptoexchange.devbot.handler.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import tgb.cryptoexchange.devbot.constants.CallbackQueryId;
import tgb.cryptoexchange.devbot.constants.DevBotUserState;
import tgb.cryptoexchange.tgcommon.keyboard.InlineButton;
import tgb.cryptoexchange.tgcommon.keyboard.KeyboardBuilder;
import tgb.cryptoexchange.tgcommon.keyboard.PressedInlineButton;
import tgb.cryptoexchange.tgcommon.service.RedisUserStateService;
import tgb.cryptoexchange.tgcommon.service.sender.EditTextMessage;
import tgb.cryptoexchange.tgcommon.service.sender.MessageTypeResolver;
import tgb.cryptoexchange.tgcommon.service.sender.ResponseSender;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewUserCallbackHandlerTest {

    @Mock
    private ResponseSender responseSender;

    @Mock
    private RedisUserStateService redisUserStateService;

    @Mock
    private KeyboardBuilder keyboardBuilder;

    @InjectMocks
    private NewUserCallbackHandler newUserCallbackHandler;

    @Captor
    private ArgumentCaptor<List<InlineButton>> buttonsCaptor;

    @ParameterizedTest
    @CsvSource({
            "123456789,55423",
            "987654321,123"
    })
    void handleShouldHandleWithPassedInlinePressedButton(Long chatId, Integer messageId) {
        Message message = Mockito.mock(Message.class);
        when(message.getMessageId()).thenReturn(messageId);
        PressedInlineButton button = Mockito.mock(PressedInlineButton.class);
        when(button.getChatId()).thenReturn(chatId);
        when(button.getMessage()).thenReturn(message);
        MessageTypeResolver messageTypeResolver = Mockito.mock(MessageTypeResolver.class);
        when(responseSender.to(chatId)).thenReturn(messageTypeResolver);
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        EditTextMessage editTextMessage = Mockito.mock(EditTextMessage.class);
        when(messageTypeResolver.editText(eq(messageId), textCaptor.capture())).thenReturn(editTextMessage);
        when(editTextMessage.replyKeyboard(any(InlineKeyboardMarkup.class))).thenReturn(editTextMessage);

        when(keyboardBuilder.buildInline(buttonsCaptor.capture())).thenReturn(new InlineKeyboardMarkup());

        newUserCallbackHandler.handle(button);

        verify(redisUserStateService).save(chatId, DevBotUserState.NEW_USER.getState());
        verify(editTextMessage).send();
        List<InlineButton> buttons = buttonsCaptor.getValue();
        assertAll(
                () -> assertEquals(1, buttons.size()),
                () -> assertEquals("Назад", buttons.getFirst().getText()),
                () -> assertTrue(buttons.getFirst().getData().startsWith(CallbackQueryId.BACK_TO_AUTH_MENU.name()))
        );
    }

    @Test
    void getIdShouldReturnId() {
        assertEquals(CallbackQueryId.NEW_USER.name(), newUserCallbackHandler.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {123456789L, 987654321L, 1L})
    void hasAccessShouldReturnTrue(Long chatId) {
        assertTrue(newUserCallbackHandler.hasAccess(chatId));
    }

}