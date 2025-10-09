package tgb.cryptoexchange.devbot.handler.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import tgb.cryptoexchange.devbot.constants.CallbackQueryId;
import tgb.cryptoexchange.devbot.service.AuthMenuService;
import tgb.cryptoexchange.devbot.service.AuthService;
import tgb.cryptoexchange.tgcommon.exception.TelegramCommonException;
import tgb.cryptoexchange.tgcommon.keyboard.PressedInlineButton;
import tgb.cryptoexchange.tgcommon.service.sender.MessageTypeResolver;
import tgb.cryptoexchange.tgcommon.service.sender.ResponseSender;
import tgb.cryptoexchange.tgcommon.service.sender.TextMessage;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteUserProcessHandlerTest {

    @Mock
    private AuthService authService;

    @Mock
    private ResponseSender responseSender;

    @Mock
    private AuthMenuService authMenuService;

    @InjectMocks
    private DeleteUserProcessHandler deleteUserProcessHandler;

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
        TextMessage textMessage = Mockito.mock(TextMessage.class);
        when(messageTypeResolver.message(textCaptor.capture())).thenReturn(textMessage);

        deleteUserProcessHandler.handle(button);

        verify(authService).delete(username);
        verify(messageTypeResolver).delete(messageId);
        verify(textMessage).send();
        assertEquals("Пользователь <b>" + username + "</b> был удален.", textCaptor.getValue());
        verify(authMenuService).sendUsers(chatId, null, CallbackQueryId.DELETE_USER_CONFIRM);
    }


    @Test
    void handleShouldThrowTelegramCommonException() {
        PressedInlineButton pressedInlineButton = Mockito.mock(PressedInlineButton.class);
        when(pressedInlineButton.getArgument(1)).thenReturn(Optional.empty());
        assertThrows(TelegramCommonException.class, () -> deleteUserProcessHandler.handle(pressedInlineButton));
    }

    @Test
    void getIdShouldReturnId() {
        assertEquals(CallbackQueryId.DELETE_USER_PROCESS.name(), deleteUserProcessHandler.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {123456789L, 987654321L, 1L})
    void hasAccessShouldReturnTrue(Long chatId) {
        assertTrue(deleteUserProcessHandler.hasAccess(chatId));
    }
}