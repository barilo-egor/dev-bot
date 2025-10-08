package tgb.cryptoexchange.devbot.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import tgb.cryptoexchange.devbot.constants.CallbackQueryId;
import tgb.cryptoexchange.tgcommon.keyboard.InlineButton;
import tgb.cryptoexchange.tgcommon.keyboard.KeyboardBuilder;
import tgb.cryptoexchange.tgcommon.keyboard.PressedInlineButton;
import tgb.cryptoexchange.tgcommon.service.sender.EditTextMessage;
import tgb.cryptoexchange.tgcommon.service.sender.MessageTypeResolver;
import tgb.cryptoexchange.tgcommon.service.sender.ResponseSender;
import tgb.cryptoexchange.tgcommon.service.sender.TextMessage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartHandlerTest {

    @Mock
    private ResponseSender responseSender;

    @Mock
    private KeyboardBuilder keyboardBuilder;

    @InjectMocks
    private StartHandler startHandler;

    @Captor
    private ArgumentCaptor<List<InlineButton>> buttonsCaptor;

    @ParameterizedTest
    @ValueSource(longs = {
            123456789L,
            987654321L
    })
    void handleShouldSendMenuToChatId(Long chatId) {
        Message message = mock(Message.class);
        when(message.getChatId()).thenReturn(chatId);

        MessageTypeResolver messageTypeResolver = mock(MessageTypeResolver.class);
        TextMessage textMessage = mock(TextMessage.class);
        when(responseSender.to(chatId)).thenReturn(messageTypeResolver);
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        when(messageTypeResolver.message(textCaptor.capture())).thenReturn(textMessage);
        when(textMessage.replyKeyboard(any(InlineKeyboardMarkup.class))).thenReturn(textMessage);
        when(keyboardBuilder.buildInline(buttonsCaptor.capture())).thenReturn(new InlineKeyboardMarkup());

        startHandler.handle(message);

        List<InlineButton> buttons = buttonsCaptor.getValue();
        assertAll(
            () -> assertEquals("Меню.", textCaptor.getValue()),
            () -> assertEquals(1, buttons.size()),
            () -> assertEquals("Сервис аутентификации", buttons.getFirst().getText()),
            () -> assertEquals(CallbackQueryId.AUTH.name(), buttons.getFirst().getData())
        );
        verify(textMessage).send();
    }

    @Test
    void getSlashCommandShouldReturnStart() {
        assertEquals("/start", startHandler.getSlashCommand());
    }

    @ParameterizedTest
    @ValueSource(longs = {
            123456789L,
            987654321L
    })
    void hasAccessShouldReturnTrue(Long chatId) {
        assertTrue(startHandler.hasAccess(chatId));
    }

    @ParameterizedTest
    @CsvSource({
            "123456789,53252",
            "987654321,124"
    })
    void handleShouldEditMessageToChatId(Long chatId, Integer messageId) {
        PressedInlineButton button = mock(PressedInlineButton.class);
        Message message = mock(Message.class);
        when(message.getMessageId()).thenReturn(messageId);
        when(button.getChatId()).thenReturn(chatId);
        when(button.getMessage()).thenReturn(message);


        MessageTypeResolver messageTypeResolver = mock(MessageTypeResolver.class);
        EditTextMessage editTextMessage = mock(EditTextMessage.class);
        when(responseSender.to(chatId)).thenReturn(messageTypeResolver);
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        when(messageTypeResolver.editText(eq(messageId), textCaptor.capture())).thenReturn(editTextMessage);
        when(editTextMessage.replyKeyboard(any(InlineKeyboardMarkup.class))).thenReturn(editTextMessage);
        when(keyboardBuilder.buildInline(buttonsCaptor.capture())).thenReturn(new InlineKeyboardMarkup());

        startHandler.handle(button);

        List<InlineButton> buttons = buttonsCaptor.getValue();
        assertAll(
                () -> assertEquals("Меню.", textCaptor.getValue()),
                () -> assertEquals(1, buttons.size()),
                () -> assertEquals("Сервис аутентификации", buttons.getFirst().getText()),
                () -> assertEquals(CallbackQueryId.AUTH.name(), buttons.getFirst().getData())
        );
        verify(editTextMessage).send();
    }

    @Test
    void getIdShouldReturnId() {
        assertEquals(CallbackQueryId.BACK_TO_MAIN_MENU.name(), startHandler.getId());
    }
}