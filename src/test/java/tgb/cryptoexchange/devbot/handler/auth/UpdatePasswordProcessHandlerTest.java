package tgb.cryptoexchange.devbot.handler.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.telegram.telegrambots.meta.api.objects.Message;
import tgb.cryptoexchange.devbot.constants.CallbackQueryId;
import tgb.cryptoexchange.devbot.service.AuthMenuService;
import tgb.cryptoexchange.devbot.service.AuthService;
import tgb.cryptoexchange.devbot.service.PasswordGenerator;
import tgb.cryptoexchange.tgcommon.exception.TelegramCommonException;
import tgb.cryptoexchange.tgcommon.keyboard.PressedInlineButton;
import tgb.cryptoexchange.tgcommon.service.sender.MessageTypeResolver;
import tgb.cryptoexchange.tgcommon.service.sender.ResponseSender;
import tgb.cryptoexchange.tgcommon.service.sender.TextMessage;
import tgb.cryptoexchange.web.ApiResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdatePasswordProcessHandlerTest {

    @Mock
    private AuthService authService;

    @Mock
    private ResponseSender responseSender;

    @Mock
    private AuthMenuService authMenuService;

    @Mock
    private PasswordGenerator passwordGenerator;

    @InjectMocks
    private UpdatePasswordProcessHandler updatePasswordProcessHandler;

    @Test
    void handleShouldThrowTelegramCommonExceptionIfNoArgument() {
        PressedInlineButton button = mock(PressedInlineButton.class);
        when(button.getArgument(1)).thenReturn(Optional.empty());
        assertThrows(TelegramCommonException.class, () -> updatePasswordProcessHandler.handle(button));
    }

    @ParameterizedTest
    @CsvSource({
            "123456789,423112,some-username,newPassword12345!@#",
            "987654321,423,darkAngel,aasdQ!@#q123"
    })
    void handleShouldSendSuccessPatch(Long chatId, Integer messageId, String username, String password) {
        var button = mock(PressedInlineButton.class);
        when(button.getArgument(1)).thenReturn(Optional.of(username));
        when(button.getChatId()).thenReturn(chatId);
        Message message = mock(Message.class);
        when(message.getMessageId()).thenReturn(messageId);
        when(button.getMessage()).thenReturn(message);
        when(passwordGenerator.generate(32)).thenReturn(password);

        MessageTypeResolver messageTypeResolver = mock(MessageTypeResolver.class);
        when(responseSender.to(chatId)).thenReturn(messageTypeResolver);
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        TextMessage textMessage = mock(TextMessage.class);
        when(messageTypeResolver.message(textCaptor.capture())).thenReturn(textMessage);

        updatePasswordProcessHandler.handle(button);

        verify(authService).patch(username, password);
        verify(messageTypeResolver).delete(messageId);
        verify(textMessage).send();
        verify(authMenuService).sendUsers(chatId, null, CallbackQueryId.UPDATE_PASSWORD_CONFIRM);
        assertEquals(
                "Пользователю <b>" + username + "</b> был обновлен пароль.\nПароль: <code>" + password + "</code>",
                textCaptor.getValue()
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid password",
            "invalid username"
    })
    void handleShouldSendBadRequestMessage(String badRequestMessage) {
        PressedInlineButton button = mock(PressedInlineButton.class);
        when(button.getChatId()).thenReturn(123456789L);
        when(button.getArgument(1)).thenReturn(Optional.of("username"));
        when(passwordGenerator.generate(32)).thenReturn("password");
        WebClientResponseException.BadRequest badRequest = mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAs(ApiResponse.class)).thenReturn(ApiResponse.error(
                ApiResponse.Error.builder().message(badRequestMessage).build())
        );
        doThrow(badRequest).when(authService).patch(anyString(), anyString());

        MessageTypeResolver messageTypeResolver = mock(MessageTypeResolver.class);
        when(responseSender.to(anyLong())).thenReturn(messageTypeResolver);
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        TextMessage textMessage = mock(TextMessage.class);
        when(messageTypeResolver.message(textCaptor.capture())).thenReturn(textMessage);

        updatePasswordProcessHandler.handle(button);

        verify(textMessage).send();
        assertEquals(badRequestMessage, textCaptor.getValue());
        verify(messageTypeResolver, times(0)).delete(anyInt());
    }

    @Test
    void getIdShouldReturnId() {
        assertEquals(CallbackQueryId.UPDATE_PASSWORD_PROCESS.name(), updatePasswordProcessHandler.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {123456789L, 987654321L, 1L})
    void hasAccessShouldReturnTrue(Long chatId) {
        assertTrue(updatePasswordProcessHandler.hasAccess(chatId));
    }

}