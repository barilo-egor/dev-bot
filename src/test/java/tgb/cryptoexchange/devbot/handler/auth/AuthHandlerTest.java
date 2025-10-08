package tgb.cryptoexchange.devbot.handler.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import tgb.cryptoexchange.devbot.constants.CallbackQueryId;
import tgb.cryptoexchange.tgcommon.keyboard.KeyboardBuilder;
import tgb.cryptoexchange.tgcommon.keyboard.PressedInlineButton;
import tgb.cryptoexchange.tgcommon.service.sender.EditTextMessage;
import tgb.cryptoexchange.tgcommon.service.sender.MessageTypeResolver;
import tgb.cryptoexchange.tgcommon.service.sender.ResponseSender;
import tgb.cryptoexchange.tgcommon.service.sender.TextMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthHandlerTest {

    @Mock
    private ResponseSender responseSender;

    @Spy
    private KeyboardBuilder keyboardBuilder;

    @InjectMocks
    private AuthHandler authHandler;

    @ParameterizedTest
    @ValueSource(longs = {123456789L, 987654321L, 1L})
    void hasAccessShouldReturnTrue(Long chatId) {
        assertTrue(authHandler.hasAccess(chatId));
    }

    @Test
    void getIdShouldReturnId() {
        assertEquals(CallbackQueryId.AUTH.name(), authHandler.getId());
    }

    @ParameterizedTest
    @CsvSource({
            "123456789, 55421",
            "987654321, 244"
    })
    void handleShouldEditMessageToPassedChatId(Long chatId, Integer messageId) {
        MessageTypeResolver messageTypeResolver = Mockito.mock(MessageTypeResolver.class);
        when(responseSender.to(chatId)).thenReturn(messageTypeResolver);
        ArgumentCaptor<String> textArgumentCaptor = ArgumentCaptor.forClass(String.class);
        EditTextMessage editTextMessage = Mockito.mock(EditTextMessage.class);
        when(messageTypeResolver.editText(eq(messageId), textArgumentCaptor.capture())).thenReturn(editTextMessage);
        ArgumentCaptor<InlineKeyboardMarkup> keyboardArgumentCaptor = ArgumentCaptor.forClass(InlineKeyboardMarkup.class);
        when(editTextMessage.replyKeyboard(keyboardArgumentCaptor.capture())).thenReturn(editTextMessage);
        authHandler.handle(chatId, messageId);
        verify(editTextMessage).send();
        InlineKeyboardMarkup keyboard = keyboardArgumentCaptor.getValue();
        assertAll(
            () -> assertEquals("Меню сервиса аутентификации.", textArgumentCaptor.getValue()),
                () -> assertEquals(4, keyboard.getKeyboard().size()),
                () -> assertEquals("Новый пользователь", keyboard.getKeyboard().getFirst().getFirst().getText()),
                () -> assertEquals(CallbackQueryId.NEW_USER.name(), keyboard.getKeyboard().getFirst().getFirst().getCallbackData()),
                () -> assertEquals("Удалить пользователя", keyboard.getKeyboard().get(1).getFirst().getText()),
                () -> assertEquals(CallbackQueryId.DELETE_USER.name(), keyboard.getKeyboard().get(1).getFirst().getCallbackData()),
                () -> assertEquals("Обновить пароль", keyboard.getKeyboard().get(2).getFirst().getText()),
                () -> assertEquals(CallbackQueryId.UPDATE_PASSWORD.name(), keyboard.getKeyboard().get(2).getFirst().getCallbackData()),
                () -> assertEquals("Назад", keyboard.getKeyboard().get(3).getFirst().getText()),
                () -> assertEquals(CallbackQueryId.BACK_TO_MAIN_MENU.name(), keyboard.getKeyboard().get(3).getFirst().getCallbackData())
        );
    }

    @ParameterizedTest
    @ValueSource(longs = {
            123456789L, 987654321L
    })
    void handleShouldSendMessageToPassedChatId(Long chatId) {
        MessageTypeResolver messageTypeResolver = Mockito.mock(MessageTypeResolver.class);
        when(responseSender.to(chatId)).thenReturn(messageTypeResolver);
        ArgumentCaptor<String> textArgumentCaptor = ArgumentCaptor.forClass(String.class);
        TextMessage textMessage = Mockito.mock(TextMessage.class);
        when(messageTypeResolver.message(textArgumentCaptor.capture())).thenReturn(textMessage);
        ArgumentCaptor<InlineKeyboardMarkup> keyboardArgumentCaptor = ArgumentCaptor.forClass(InlineKeyboardMarkup.class);
        when(textMessage.replyKeyboard(keyboardArgumentCaptor.capture())).thenReturn(textMessage);
        authHandler.handle(chatId);
        verify(textMessage).send();
        InlineKeyboardMarkup keyboard = keyboardArgumentCaptor.getValue();
        assertAll(
                () -> assertEquals("Меню сервиса аутентификации.", textArgumentCaptor.getValue()),
                () -> assertEquals(4, keyboard.getKeyboard().size()),
                () -> assertEquals("Новый пользователь", keyboard.getKeyboard().getFirst().getFirst().getText()),
                () -> assertEquals(CallbackQueryId.NEW_USER.name(), keyboard.getKeyboard().getFirst().getFirst().getCallbackData()),
                () -> assertEquals("Удалить пользователя", keyboard.getKeyboard().get(1).getFirst().getText()),
                () -> assertEquals(CallbackQueryId.DELETE_USER.name(), keyboard.getKeyboard().get(1).getFirst().getCallbackData()),
                () -> assertEquals("Обновить пароль", keyboard.getKeyboard().get(2).getFirst().getText()),
                () -> assertEquals(CallbackQueryId.UPDATE_PASSWORD.name(), keyboard.getKeyboard().get(2).getFirst().getCallbackData()),
                () -> assertEquals("Назад", keyboard.getKeyboard().get(3).getFirst().getText()),
                () -> assertEquals(CallbackQueryId.BACK_TO_MAIN_MENU.name(), keyboard.getKeyboard().get(3).getFirst().getCallbackData())
        );
    }

    @ParameterizedTest
    @CsvSource({
            "123456789, 55421",
            "987654321, 244"
    })
    void handleShouldHandleWithPassedPressedInlineButton(Long chatId, Integer messageId) {
        PressedInlineButton pressedInlineButton = new PressedInlineButton();
        Chat chat = new Chat();
        chat.setId(chatId);
        Message message = new Message();
        message.setMessageId(messageId);
        message.setChat(chat);
        pressedInlineButton.setMessage(message);

        MessageTypeResolver messageTypeResolver = Mockito.mock(MessageTypeResolver.class);
        when(responseSender.to(chatId)).thenReturn(messageTypeResolver);
        ArgumentCaptor<String> textArgumentCaptor = ArgumentCaptor.forClass(String.class);
        EditTextMessage editTextMessage = Mockito.mock(EditTextMessage.class);
        when(messageTypeResolver.editText(eq(messageId), textArgumentCaptor.capture())).thenReturn(editTextMessage);
        ArgumentCaptor<InlineKeyboardMarkup> keyboardArgumentCaptor = ArgumentCaptor.forClass(InlineKeyboardMarkup.class);
        when(editTextMessage.replyKeyboard(keyboardArgumentCaptor.capture())).thenReturn(editTextMessage);

        authHandler.handle(pressedInlineButton);
        verify(editTextMessage).send();
    }
}