package tgb.cryptoexchange.devbot.handler.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.devbot.exception.AuthException;
import tgb.cryptoexchange.tgcommon.service.sender.MessageTypeResolver;
import tgb.cryptoexchange.tgcommon.service.sender.ResponseSender;
import tgb.cryptoexchange.tgcommon.service.sender.TextMessage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthExceptionHandlerTest {

    @Mock
    private ResponseSender responseSender;

    @InjectMocks
    private AuthExceptionHandler authExceptionHandler;

    @Test
    void isInstanceShouldReturnTrue() {
        assertTrue(authExceptionHandler.isInstance(new AuthException("")));
    }

    @Test
    void isInstanceShouldReturnFalse() {
        assertFalse(authExceptionHandler.isInstance(new RuntimeException("")));
    }

    @ParameterizedTest
    @ValueSource(longs = {
            123456789L,
            987654321L
    })
    void handleShouldSendTimeStamp(Long chatId) {
        MessageTypeResolver messageTypeResolver = mock(MessageTypeResolver.class);
        when(responseSender.to(chatId)).thenReturn(messageTypeResolver);
        TextMessage textMessage = mock(TextMessage.class);
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        when(messageTypeResolver.message(textCaptor.capture())).thenReturn(textMessage);

        authExceptionHandler.handle(chatId, new AuthException(""));
        String actualMessage = textCaptor.getValue();
        assertTrue(actualMessage.startsWith("Ошибка при выполнении запроса.\n<code>"));
        assertTrue(actualMessage.endsWith("</code>"));
        verify(textMessage).send();
    }
}