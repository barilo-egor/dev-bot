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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteUserConfirmHandlerTest {

    @Mock
    private ResponseSender responseSender;

    @Mock
    private KeyboardBuilder keyboardBuilder;

    @InjectMocks
    private DeleteUserConfirmHandler deleteUserConfirmHandler;

    @Captor
    private ArgumentCaptor<List<InlineButton>> keyboardCaptor;

    @ParameterizedTest
    @CsvSource({
            "Qwe123,123456789,55423",
            "Super-username,987654321,123"
    })
    void handleShouldAskConfirmForUsername(String username, Long chatId, Integer messageId) {
        PressedInlineButton pressedInlineButton = Mockito.mock(PressedInlineButton.class);
        when(pressedInlineButton.getArgument(1)).thenReturn(Optional.of(username));
        when(pressedInlineButton.getChatId()).thenReturn(chatId);
        Message message = Mockito.mock(Message.class);
        when(pressedInlineButton.getMessage()).thenReturn(message);
        when(message.getMessageId()).thenReturn(messageId);
        MessageTypeResolver messageTypeResolver = Mockito.mock(MessageTypeResolver.class);
        when(responseSender.to(chatId)).thenReturn(messageTypeResolver);
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        EditTextMessage editTextMessage = Mockito.mock(EditTextMessage.class);
        when(messageTypeResolver.editText(eq(messageId), textCaptor.capture())).thenReturn(editTextMessage);
        when(editTextMessage.replyKeyboard(any(InlineKeyboardMarkup.class))).thenReturn(editTextMessage);
        when(keyboardBuilder.buildInline(anyInt(), keyboardCaptor.capture())).thenReturn(new InlineKeyboardMarkup());
        deleteUserConfirmHandler.handle(pressedInlineButton);
        verify(editTextMessage).send();
        List<InlineButton> buttons = keyboardCaptor.getValue();
        assertAll(
                () -> assertEquals("Вы действительно хотите удалить пользователя <b>" + username + "</b>?", textCaptor.getValue()),
                () -> assertEquals(2, buttons.size()),
                () -> assertEquals("Да", buttons.getFirst().getText()),
                () -> assertTrue(buttons.getFirst().getData().contains(username)),
                () -> assertTrue(buttons.getFirst().getData().startsWith(CallbackQueryId.DELETE_USER_PROCESS.name())),
                () -> assertEquals("Нет", buttons.get(1).getText()),
                () -> assertEquals(CallbackQueryId.BACK_TO_DELETE_USER_MENU.name(), buttons.get(1).getData())
        );
    }

    @Test
    void handleShouldThrowTelegramCommonException() {
        PressedInlineButton pressedInlineButton = Mockito.mock(PressedInlineButton.class);
        when(pressedInlineButton.getArgument(1)).thenReturn(Optional.empty());
        assertThrows(TelegramCommonException.class, () -> deleteUserConfirmHandler.handle(pressedInlineButton));
    }

    @Test
    void getIdShouldReturnId() {
        assertEquals(CallbackQueryId.DELETE_USER_CONFIRM.name(), deleteUserConfirmHandler.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {123456789L, 987654321L, 1L})
    void hasAccessShouldReturnTrue(Long chatId) {
        assertTrue(deleteUserConfirmHandler.hasAccess(chatId));
    }
}