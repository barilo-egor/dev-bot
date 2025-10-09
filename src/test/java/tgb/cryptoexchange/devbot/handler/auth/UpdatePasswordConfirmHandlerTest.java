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
import tgb.cryptoexchange.tgcommon.exception.TelegramCommonException;
import tgb.cryptoexchange.tgcommon.keyboard.InlineButton;
import tgb.cryptoexchange.tgcommon.keyboard.KeyboardBuilder;
import tgb.cryptoexchange.tgcommon.keyboard.PressedInlineButton;
import tgb.cryptoexchange.tgcommon.service.sender.EditTextMessage;
import tgb.cryptoexchange.tgcommon.service.sender.MessageTypeResolver;
import tgb.cryptoexchange.tgcommon.service.sender.ResponseSender;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePasswordConfirmHandlerTest {

    @Mock
    private ResponseSender responseSender;

    @Mock
    private KeyboardBuilder keyboardBuilder;

    @InjectMocks
    private UpdatePasswordConfirmHandler updatePasswordConfirmHandler;

    @Captor
    private ArgumentCaptor<List<InlineButton>> buttonsCaptor;

    @ParameterizedTest
    @CsvSource({
            "Qwe123,123456789,55423",
            "Super-username,987654321,123"
    })
    void handleShouldHandleWithPassedInlinePressedButton(String username, Long chatId, Integer messageId) {
        PressedInlineButton button = Mockito.mock(PressedInlineButton.class);
        when(button.getArgument(1)).thenReturn(Optional.of(username));
        when(button.getChatId()).thenReturn(chatId);
        Message message = Mockito.mock(Message.class);
        when(message.getMessageId()).thenReturn(messageId);
        when(button.getMessage()).thenReturn(message);
        MessageTypeResolver messageTypeResolver = Mockito.mock(MessageTypeResolver.class);
        when(responseSender.to(chatId)).thenReturn(messageTypeResolver);
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        EditTextMessage editTextMessage = Mockito.mock(EditTextMessage.class);
        when(messageTypeResolver.editText(eq(messageId), textCaptor.capture())).thenReturn(editTextMessage);
        when(keyboardBuilder.buildInline(eq(2), buttonsCaptor.capture())).thenReturn(new InlineKeyboardMarkup());
        when(editTextMessage.replyKeyboard(any(InlineKeyboardMarkup.class))).thenReturn(editTextMessage);

        updatePasswordConfirmHandler.handle(button);

        verify(editTextMessage).send();
        List<InlineButton> buttons = buttonsCaptor.getValue();
        assertAll(
                () -> assertEquals("Вы действительно хотите обновить пароль пользователю <b>" + username + "</b>?", textCaptor.getValue()),
                () -> assertEquals(2, buttons.size()),
                () -> assertEquals("Да", buttons.getFirst().getText()),
                () -> assertTrue(buttons.getFirst().getData().startsWith(CallbackQueryId.UPDATE_PASSWORD_PROCESS.name())),
                () -> assertTrue(buttons.getFirst().getData().contains(username)),
                () -> assertEquals("Нет", buttons.get(1).getText()),
                () -> assertTrue(buttons.get(1).getData().startsWith(CallbackQueryId.BACK_TO_DELETE_USER_MENU.name()))
        );

    }


    @Test
    void handleShouldThrowTelegramCommonException() {
        PressedInlineButton pressedInlineButton = Mockito.mock(PressedInlineButton.class);
        when(pressedInlineButton.getArgument(1)).thenReturn(Optional.empty());
        assertThrows(TelegramCommonException.class, () -> updatePasswordConfirmHandler.handle(pressedInlineButton));
    }

    @Test
    void getIdShouldReturnId() {
        assertEquals(CallbackQueryId.UPDATE_PASSWORD_CONFIRM.name(), updatePasswordConfirmHandler.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {123456789L, 987654321L, 1L})
    void hasAccessShouldReturnTrue(Long chatId) {
        assertTrue(updatePasswordConfirmHandler.hasAccess(chatId));
    }
}