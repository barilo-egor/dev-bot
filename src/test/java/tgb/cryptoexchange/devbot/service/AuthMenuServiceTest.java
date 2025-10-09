package tgb.cryptoexchange.devbot.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import tgb.cryptoexchange.devbot.constants.CallbackQueryId;
import tgb.cryptoexchange.tgcommon.keyboard.InlineButton;
import tgb.cryptoexchange.tgcommon.keyboard.KeyboardBuilder;
import tgb.cryptoexchange.tgcommon.service.sender.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthMenuServiceTest {

    @Mock
    private ResponseSender responseSender;

    @Mock
    private AuthService authService;

    @Mock
    private KeyboardBuilder keyboardBuilder;

    @InjectMocks
    private AuthMenuService authMenuService;

    @Captor
    private ArgumentCaptor<List<InlineButton>> buttonsCaptor;

    @ParameterizedTest
    @ValueSource(strings = {
            "AUTH", "DELETE_USER"
    })
    void sendUsersShouldThrowIllegalArgumentException(CallbackQueryId callbackQueryId) {
        assertThrows(IllegalStateException.class, () -> authMenuService.sendUsers(null, null, callbackQueryId));
    }

    @ParameterizedTest
    @CsvSource({
            "123456789,15352,DELETE_USER_CONFIRM,username1;username2;username3",
            "987654321,53,UPDATE_PASSWORD_CONFIRM,username1"
    })
    void sendUsersShouldSendEditText(Long chatId, Integer messageId, CallbackQueryId callbackQueryId, String usernamesString) {
        List<String> usernames = Arrays.asList(usernamesString.split(";"));
        MessageTypeResolver messageTypeResolver = mock(MessageTypeResolver.class);
        when(responseSender.to(chatId)).thenReturn(messageTypeResolver);
        Action action = mock(Action.class);
        when(messageTypeResolver.action()).thenReturn(action);
        when(authService.getUsernames()).thenReturn(usernames);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        EditTextMessage editTextMessage = mock(EditTextMessage.class);
        when(messageTypeResolver.editText(eq(messageId), messageCaptor.capture())).thenReturn(editTextMessage);
        when(editTextMessage.replyKeyboard(any(InlineKeyboardMarkup.class))).thenReturn(editTextMessage);
        when(keyboardBuilder.buildInline(eq(2), buttonsCaptor.capture())).thenReturn(new InlineKeyboardMarkup());

        authMenuService.sendUsers(chatId, messageId, callbackQueryId);

        verify(action).typing();
        verify(editTextMessage).send();
        String expectedMessage = CallbackQueryId.DELETE_USER_CONFIRM.equals(callbackQueryId)
                ? "Выберите пользователя для удаления."
                : "Выберите пользователя для обновления пароля.";
        List<InlineButton> buttons = buttonsCaptor.getValue();
        assertAll(
                () -> assertEquals(expectedMessage, messageCaptor.getValue()),
                () -> assertEquals(usernames.size() + 1, buttons.size())
        );
        for (int i = 0; i < usernames.size(); i++) {
            String username = usernames.get(i);
            InlineButton button = buttons.get(i);
            assertEquals(username, button.getText());
            assertTrue(button.getData().startsWith(callbackQueryId.name()));
            assertTrue(button.getData().contains(username));
        }
        InlineButton backButton = buttons.get(usernames.size());
        assertEquals("Назад", backButton.getText());
        assertEquals(CallbackQueryId.BACK_TO_AUTH_MENU.name(), backButton.getData());
    }

    @ParameterizedTest
    @CsvSource({
            "123456789,DELETE_USER_CONFIRM,username1;username2;username3",
            "987654321,UPDATE_PASSWORD_CONFIRM,username1"
    })
    void sendUsersShouldSendMessage(Long chatId, CallbackQueryId callbackQueryId, String usernamesString) {
        List<String> usernames = Arrays.asList(usernamesString.split(";"));
        MessageTypeResolver messageTypeResolver = mock(MessageTypeResolver.class);
        when(responseSender.to(chatId)).thenReturn(messageTypeResolver);
        Action action = mock(Action.class);
        when(messageTypeResolver.action()).thenReturn(action);
        when(authService.getUsernames()).thenReturn(usernames);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        TextMessage textMessage = mock(TextMessage.class);
        when(messageTypeResolver.message(messageCaptor.capture())).thenReturn(textMessage);
        when(textMessage.replyKeyboard(any(InlineKeyboardMarkup.class))).thenReturn(textMessage);
        when(keyboardBuilder.buildInline(eq(2), buttonsCaptor.capture())).thenReturn(new InlineKeyboardMarkup());

        authMenuService.sendUsers(chatId, null, callbackQueryId);

        verify(action).typing();
        verify(textMessage).send();
        String expectedMessage = CallbackQueryId.DELETE_USER_CONFIRM.equals(callbackQueryId)
                ? "Выберите пользователя для удаления."
                : "Выберите пользователя для обновления пароля.";
        List<InlineButton> buttons = buttonsCaptor.getValue();
        assertAll(
                () -> assertEquals(expectedMessage, messageCaptor.getValue()),
                () -> assertEquals(usernames.size() + 1, buttons.size())
        );
        for (int i = 0; i < usernames.size(); i++) {
            String username = usernames.get(i);
            InlineButton button = buttons.get(i);
            assertEquals(username, button.getText());
            assertTrue(button.getData().startsWith(callbackQueryId.name()));
            assertTrue(button.getData().contains(username));
        }
        InlineButton backButton = buttons.get(usernames.size());
        assertEquals("Назад", backButton.getText());
        assertEquals(CallbackQueryId.BACK_TO_AUTH_MENU.name(), backButton.getData());
    }
}