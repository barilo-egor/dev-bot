package tgb.cryptoexchange.devbot.service;

import org.apache.http.HttpHeaders;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.devbot.exception.AuthException;
import tgb.cryptoexchange.devbot.exception.NoResponseException;
import tgb.cryptoexchange.web.ApiResponse;
import tgb.cryptoexchange.web.AuthLoginService;

import java.util.List;
import java.util.Objects;


@Service
public class AuthService {

    private static final String BEARER = "Bearer ";

    private final WebClient webClient;

    private final AuthLoginService authLoginService;

    public AuthService(WebClient authWebClient,
                       AuthLoginService authLoginService) {
        this.webClient = authWebClient;
        this.authLoginService = authLoginService;
    }

    public boolean isUsernameFree(String username) {
        return !getUsernames().contains(username);
    }

    public List<String> getUsernames() {
        ApiResponse<List<String>> response = webClient.get()
                .header(HttpHeaders.AUTHORIZATION, BEARER + authLoginService.login())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<String>>>() {
                })
                .block();
        if (Objects.isNull(response)) {
            throw new NoResponseException("No response.");
        }
        if (response.isSuccess()) {
            return response.getData();
        }
        ApiResponse.Error error = response.getError();
        throw new AuthException("Код ошибки: " + error.getCode() + ". Сообщение: " + error.getMessage());
    }

    public void register(String username, String password) throws AuthException {
        ApiResponse<String> response = webClient.post()
                .uri("/register")
                .header(HttpHeaders.AUTHORIZATION, BEARER + authLoginService.login())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new RegisterRequest(username, password))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<String>>() {
                })
                .block();
        if (Objects.isNull(response)) {
            throw new NoResponseException("No response.");
        }
        if (!response.isSuccess()) {
            ApiResponse.Error error = response.getError();
            throw new AuthException("Код ошибки: " + error.getCode() + ". Сообщение: " + error.getMessage());
        }
    }

    record RegisterRequest(String username, String password) {
    }

    public void delete(String username) {
        webClient.delete()
                .uri(uriBuilder -> uriBuilder.path("/" + username).build())
                .header(HttpHeaders.AUTHORIZATION, BEARER + authLoginService.login())
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void patch(String username, String password) {
        webClient.patch()
                .uri(uriBuilder -> uriBuilder.path("/" + username).queryParam("password", password).build())
                .header(HttpHeaders.AUTHORIZATION, BEARER + authLoginService.login())
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
