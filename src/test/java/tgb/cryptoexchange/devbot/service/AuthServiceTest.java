package tgb.cryptoexchange.devbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tgb.cryptoexchange.devbot.exception.AuthException;
import tgb.cryptoexchange.web.ApiResponse;
import tgb.cryptoexchange.web.AuthLoginService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private WebClient webClient;

    @Mock
    private ExchangeFunction exchangeFunction;

    @Mock
    private AuthLoginService authLoginService;

    private AuthService authService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .baseUrl("http://localhost")
                .build();
        authService = new AuthService(webClient, authLoginService);
    }

    @ParameterizedTest
    @CsvSource({
            "ENTITY_NOT_FOUND,Some error",
            "ENTITY_NOT_FOUND,Error"
    })
    void getUsernamesShouldThrowAuthException(ApiResponse.Error.ErrorCode code, String message) throws JsonProcessingException {
        when(authLoginService.login()).thenReturn("fake-token");
        ClientResponse response = ClientResponse
                .create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(objectMapper.writeValueAsString(
                        ApiResponse.error(ApiResponse.Error.builder().code(code).message(message).build())
                ))
                .build();
        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(response));
        assertThrows(AuthException.class, () -> authService.getUsernames(), "Код ошибки: " + code + ". Сообщение: " + message);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "username1,username2,username3",
            "username1"
    })
    void shouldReturnUsernames(String usernamesString) throws JsonProcessingException {
        List<String> usernames = Arrays.asList(usernamesString.split(","));
        when(authLoginService.login()).thenReturn("fake-token");
        ClientResponse response = ClientResponse
                .create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(objectMapper.writeValueAsString(ApiResponse.success(usernames)))
                .build();
        when(exchangeFunction.exchange(any(ClientRequest.class))).thenReturn(Mono.just(response));

        assertEquals(usernames, authService.getUsernames());
    }

    @Test
    void shouldReturnEmptyUsernames() throws JsonProcessingException {
        List<String> usernames = new ArrayList<>();
        when(authLoginService.login()).thenReturn("fake-token");
        ClientResponse response = ClientResponse
                .create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(objectMapper.writeValueAsString(ApiResponse.success(usernames)))
                .build();
        when(exchangeFunction.exchange(any(ClientRequest.class))).thenReturn(Mono.just(response));

        assertTrue(authService.getUsernames().isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "ENTITY_NOT_FOUND,Some error",
            "ENTITY_NOT_FOUND,Error"
    })
    void registerShouldThrowAuthException(ApiResponse.Error.ErrorCode code, String message) throws JsonProcessingException {
        when(authLoginService.login()).thenReturn("fake-token");
        ClientResponse response = ClientResponse
                .create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(objectMapper.writeValueAsString(
                        ApiResponse.error(ApiResponse.Error.builder().code(code).message(message).build())
                ))
                .build();
        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(response));
        assertThrows(AuthException.class, () -> authService.register("username", "password"),
                "Код ошибки: " + code + ". Сообщение: " + message);
    }


}