package tgb.cryptoexchange.devbot.handler.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class WebClientResponseExceptionHandlerTest {

    @InjectMocks
    private WebClientResponseExceptionHandler webClientResponseExceptionHandler;

    @Test
    void isInstanceShouldReturnTrue() {
        assertTrue(webClientResponseExceptionHandler.isInstance(mock(WebClientResponseException.class)));
    }

    @Test
    void isInstanceShouldReturnFalse() {
        assertFalse(webClientResponseExceptionHandler.isInstance(new RuntimeException("")));
    }

}