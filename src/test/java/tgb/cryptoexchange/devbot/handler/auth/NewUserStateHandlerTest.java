package tgb.cryptoexchange.devbot.handler.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import tgb.cryptoexchange.devbot.constants.CallbackQueryId;
import tgb.cryptoexchange.devbot.constants.DevBotUserState;
import tgb.cryptoexchange.devbot.service.AuthService;
import tgb.cryptoexchange.devbot.service.PasswordGenerator;
import tgb.cryptoexchange.tgcommon.service.RedisUserStateService;
import tgb.cryptoexchange.tgcommon.service.sender.Action;
import tgb.cryptoexchange.tgcommon.service.sender.MessageTypeResolver;
import tgb.cryptoexchange.tgcommon.service.sender.ResponseSender;
import tgb.cryptoexchange.tgcommon.service.sender.TextMessage;
import tgb.cryptoexchange.web.ApiResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewUserStateHandlerTest {

    @Mock
    private ResponseSender responseSender;

    @Mock
    private AuthService authService;

    @Mock
    private AuthHandler authHandler;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Mock
    private RedisUserStateService redisUserStateService;

    @InjectMocks
    private NewUserStateHandler newUserStateHandler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ParameterizedTest
    @CsvSource({
            "123456789,55423",
            "987654321,123"
    })
    void handleShouldCallAuthHandlerIfUpdateHasCallbackQueryWithBackData(Long chatId, Integer messageId) {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message message = mock(Message.class);
        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn(CallbackQueryId.BACK_TO_AUTH_MENU.name());
        when(callbackQuery.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);
        when(message.getMessageId()).thenReturn(messageId);
        newUserStateHandler.handle(update);
        verify(authHandler).handle(chatId, messageId);
        verify(redisUserStateService).delete(chatId);
        verify(responseSender, times(0)).to(anyLong());
    }

    @ParameterizedTest
    @CsvSource({
            "123456789",
            "987654321"
    })
    void handleShouldSkipCallAuthHandlerIfUpdateHasCallbackQueryWithNotBackData(Long chatId) {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        User user = mock(User.class);
        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("someData");
        when(callbackQuery.getFrom()).thenReturn(user);
        when(user.getId()).thenReturn(chatId);

        MessageTypeResolver messageTypeResolver = mock(MessageTypeResolver.class);
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        TextMessage textMessage = mock(TextMessage.class);
        when(messageTypeResolver.message(textCaptor.capture())).thenReturn(textMessage);
        when(responseSender.to(chatId)).thenReturn(messageTypeResolver);

        newUserStateHandler.handle(update);
        verify(authHandler, times(0)).handle(anyLong(), anyInt());
        verify(textMessage).send();
        assertEquals("Отправьте идентификатор пользователя, либо нажмите \"Назад\".", textCaptor.getValue());
    }

    @ParameterizedTest
    @CsvSource({
            "123456789",
            "987654321"
    })
    void handleShouldSkipCallAuthHandlerIfUpdateHasNoCallbackQuery(Long chatId) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        when(update.hasCallbackQuery()).thenReturn(false);
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(false);
        when(message.getChatId()).thenReturn(chatId);

        MessageTypeResolver messageTypeResolver = mock(MessageTypeResolver.class);
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        TextMessage textMessage = mock(TextMessage.class);
        when(messageTypeResolver.message(textCaptor.capture())).thenReturn(textMessage);
        when(responseSender.to(chatId)).thenReturn(messageTypeResolver);

        newUserStateHandler.handle(update);
        verify(authHandler, times(0)).handle(anyLong(), anyInt());
        verify(textMessage).send();
        assertEquals("Отправьте идентификатор пользователя, либо нажмите \"Назад\".", textCaptor.getValue());
    }

    @ParameterizedTest
    @CsvSource({
            "123456789,qwerty",
            "987654321,some-username"
    })
    void handleShouldSendMessageIdentifierTaken(Long chatId, String username) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        when(update.hasCallbackQuery()).thenReturn(false);
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn(username);
        when(message.getChatId()).thenReturn(chatId);

        MessageTypeResolver messageTypeResolver = mock(MessageTypeResolver.class);
        Action action = mock(Action.class);
        when(responseSender.to(chatId)).thenReturn(messageTypeResolver);
        when(messageTypeResolver.action()).thenReturn(action);
        when(authService.isUsernameFree(username)).thenReturn(false);
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        TextMessage textMessage = mock(TextMessage.class);
        when(messageTypeResolver.message(textCaptor.capture())).thenReturn(textMessage);

        newUserStateHandler.handle(update);

        verify(action).typing();
        verify(textMessage).send();
        assertEquals("Идентификатор уже используется.", textCaptor.getValue());
    }

    @ParameterizedTest
    @CsvSource({
            "123456789,qwerty,qwe1243$!@$qweQ",
            "987654321,some-username,ADds#1253wqA%#S"
    })
    void handleShouldSendSuccessRegister(Long chatId, String username, String password) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        when(update.hasCallbackQuery()).thenReturn(false);
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn(username);
        when(message.getChatId()).thenReturn(chatId);
        when(passwordGenerator.generate(32)).thenReturn(password);

        MessageTypeResolver messageTypeResolver = mock(MessageTypeResolver.class);
        Action action = mock(Action.class);
        when(responseSender.to(chatId)).thenReturn(messageTypeResolver);
        when(messageTypeResolver.action()).thenReturn(action);
        when(authService.isUsernameFree(username)).thenReturn(true);
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        TextMessage textMessage = mock(TextMessage.class);
        when(messageTypeResolver.message(textCaptor.capture())).thenReturn(textMessage);

        newUserStateHandler.handle(update);

        verify(action).typing();
        verify(textMessage).send();
        verify(redisUserStateService).delete(chatId);
        verify(authHandler).handle(chatId);
        assertEquals(
                "Пользователь <b>" + username + "</b> успешно зарегистрирован.\nПароль: <code>" + password + "</code>",
                textCaptor.getValue()
        );
    }

    @ParameterizedTest
    @CsvSource({
            "123456789,qwerty,qwe1243$!@$qweQ,invalid password",
            "987654321,some-username,ADds#1253wqA%#S,invalid username"
    })
    void handleShouldSendBadRequestMessage(Long chatId, String username, String password, String badRequestMessage) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        when(update.hasCallbackQuery()).thenReturn(false);
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn(username);
        when(message.getChatId()).thenReturn(chatId);
        when(passwordGenerator.generate(32)).thenReturn(password);
        WebClientResponseException.BadRequest badRequest = mock(WebClientResponseException.BadRequest.class);
        when(badRequest.getResponseBodyAs(ApiResponse.class)).thenReturn(ApiResponse.error(
                ApiResponse.Error.builder().message(badRequestMessage).build())
        );
        doThrow(badRequest).when(authService).register(username, password);

        MessageTypeResolver messageTypeResolver = mock(MessageTypeResolver.class);
        Action action = mock(Action.class);
        when(responseSender.to(chatId)).thenReturn(messageTypeResolver);
        when(messageTypeResolver.action()).thenReturn(action);
        when(authService.isUsernameFree(username)).thenReturn(true);
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        TextMessage textMessage = mock(TextMessage.class);
        when(messageTypeResolver.message(textCaptor.capture())).thenReturn(textMessage);

        newUserStateHandler.handle(update);

        verify(action).typing();
        verify(textMessage).send();
        assertEquals(badRequestMessage, textCaptor.getValue());
    }

    @Test
    void shouldReturnNewUserState() {
        assertEquals(DevBotUserState.NEW_USER.getState(), newUserStateHandler.getUserState());
    }
}