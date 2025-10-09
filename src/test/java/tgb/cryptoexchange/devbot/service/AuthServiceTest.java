package tgb.cryptoexchange.devbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.devbot.exception.AuthException;
import tgb.cryptoexchange.devbot.exception.NoResponseException;
import tgb.cryptoexchange.web.ApiResponse;
import tgb.cryptoexchange.web.AuthLoginService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    MockWebServer mockWebServer;

    AuthService authService;

    AuthLoginService authLoginService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();
        authLoginService = mock(AuthLoginService.class);
        authService = new AuthService(webClient, authLoginService);
    }

    @AfterEach
    void shutdown() throws IOException {
        mockWebServer.shutdown();
    }

    @ParameterizedTest
    @CsvSource({
            "isFreeUsername,username1",
            "DarkAngel,username1;username2;username3",
            "sureFreeUsername,"
    })
    void isUsernameFreeShouldReturnTrue(String usernameToFind, String usernamesString) throws JsonProcessingException {
        List<String> usernames;
        if (Objects.nonNull(usernamesString) && !usernamesString.isEmpty()) {
            usernames = Arrays.asList(usernamesString.split(";"));
        } else {
            usernames = new ArrayList<>();
        }
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(ApiResponse.success(usernames)))
        );
        String token = "some-token";
        when(authLoginService.login()).thenReturn(token);

        assertTrue(authService.isUsernameFree(usernameToFind));
    }

    @ParameterizedTest
    @CsvSource({
            "username1,username1",
            "username2,username1;username2;username3"
    })
    void isUsernameFreeShouldReturnFalse(String usernameToFind, String usernamesString) throws JsonProcessingException {
        List<String> usernames;
        if (Objects.nonNull(usernamesString) && !usernamesString.isEmpty()) {
            usernames = Arrays.asList(usernamesString.split(";"));
        } else {
            usernames = new ArrayList<>();
        }
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(ApiResponse.success(usernames)))
        );
        String token = "some-token";
        when(authLoginService.login()).thenReturn(token);

        assertFalse(authService.isUsernameFree(usernameToFind));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "username1",
            "username1,username2,username3",
            ""
    })
    void getUsernamesShouldReturnUsernames(String usernamesString) throws JsonProcessingException, InterruptedException {
        List<String> usernames;
        if (!usernamesString.isEmpty()) {
            usernames = Arrays.asList(usernamesString.split(","));
        } else {
            usernames = new ArrayList<>();
        }
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(ApiResponse.success(usernames)))
        );
        String token = "some-token";
        when(authLoginService.login()).thenReturn(token);

        List<String> actual = authService.getUsernames();

        assertEquals(usernames, actual);
        RecordedRequest request = mockWebServer.takeRequest();
        assertAll(
                () -> assertEquals("GET", request.getMethod()),
                () -> assertEquals("Bearer " + token, request.getHeader(HttpHeaders.AUTHORIZATION)),
                () -> assertEquals(MediaType.APPLICATION_JSON_VALUE, request.getHeader(HttpHeaders.CONTENT_TYPE))
        );
    }

    @ParameterizedTest
    @CsvSource({
            "ENTITY_NOT_FOUND,Some error",
            "ENTITY_NOT_FOUND,Error"
    })
    void getUsernamesShouldThrowAuthException(ApiResponse.Error.ErrorCode code, String message) throws JsonProcessingException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(
                        ApiResponse.error(ApiResponse.Error.builder().code(code).message(message).build())
                ))
        );
        String token = "some-token";
        when(authLoginService.login()).thenReturn(token);

        assertThrows(AuthException.class, () -> authService.getUsernames(), "Код ошибки: " + code + ". Сообщение: " + message);
    }

    @Test
    void getUsernamesShouldThrowNoResponseException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        );
        assertThrows(NoResponseException.class, () -> authService.getUsernames());
    }



    @ParameterizedTest
    @CsvSource({
            "username1,pASSword123!@#",
            "username2,qweaf!@#1r14Qqwe"
    })
    void registerShouldSendCorrectRequest(String username, String password) throws InterruptedException, JsonProcessingException {
        String expectedResponseBody = "someNewToken";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(ApiResponse.success(expectedResponseBody)))
        );
        String token = "token";
        when(authLoginService.login()).thenReturn(token);

        authService.register(username, password);

        RecordedRequest request = mockWebServer.takeRequest();

        String expectedBody = objectMapper.writeValueAsString(new AuthService.RegisterRequest(username, password));
        assertAll(
                () -> assertEquals("Bearer " + token, request.getHeader(HttpHeaders.AUTHORIZATION)),
                () -> assertEquals(MediaType.APPLICATION_JSON_VALUE, request.getHeader(HttpHeaders.CONTENT_TYPE)),
                () -> assertEquals(expectedBody, request.getBody().readUtf8()),
                () -> assertEquals("POST", request.getMethod())
        );
    }

    @ParameterizedTest
    @CsvSource({
            "ENTITY_NOT_FOUND,Some error",
            "ENTITY_NOT_FOUND,Error"
    })
    void registerShouldThrowAuthException(ApiResponse.Error.ErrorCode code, String message) throws JsonProcessingException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(
                        ApiResponse.error(ApiResponse.Error.builder().code(code).message(message).build())
                ))
        );
        String token = "some-token";
        when(authLoginService.login()).thenReturn(token);

        assertThrows(AuthException.class, () -> authService.register("username", "password"),
                "Код ошибки: " + code + ". Сообщение: " + message);
    }

    @Test
    void registerShouldThrowNoResponseException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        );
        assertThrows(NoResponseException.class, () -> authService.register("username", "password"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "username1", "qweaAS123"
    })
    void deleteShouldSendCorrectRequest(String username) throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NO_CONTENT.value())
        );
        String token = "token";
        when(authLoginService.login()).thenReturn(token);

        authService.delete(username);

        RecordedRequest request = mockWebServer.takeRequest();
        assertTrue(Objects.nonNull(request.getRequestUrl()));
        assertAll(
                () -> assertTrue(request.getRequestUrl().toString().endsWith("/" + username)),
                () -> assertEquals("Bearer " + token, request.getHeader(HttpHeaders.AUTHORIZATION)),
                () -> assertEquals("DELETE", request.getMethod())
        );
    }

    @ParameterizedTest
    @CsvSource({
            "username1,aspdRW-_.~!()414",
            "name,seqEQW142-_.~!()"
    })
    void patchShouldSendCorrectRequest(String username, String password) throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NO_CONTENT.value())
        );
        String token = "token";
        when(authLoginService.login()).thenReturn(token);

        authService.patch(username, password);

        RecordedRequest request = mockWebServer.takeRequest();
        assertTrue(Objects.nonNull(request.getRequestUrl()));
        assertAll(
                () -> assertTrue(request.getRequestUrl().toString().endsWith("/" + username + "?password=" + password)),
                () -> assertEquals("Bearer " + token, request.getHeader(HttpHeaders.AUTHORIZATION)),
                () -> assertEquals("PATCH", request.getMethod())
        );
    }
}