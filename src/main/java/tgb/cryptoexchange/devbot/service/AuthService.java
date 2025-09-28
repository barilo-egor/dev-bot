package tgb.cryptoexchange.devbot.service;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.devbot.exception.AuthException;
import tgb.cryptoexchange.devbot.exception.NoResponseException;
import tgb.cryptoexchange.web.ApiResponse;

import java.util.List;
import java.util.Objects;


@Service
public class AuthService {

    private final WebClient webClient;

    public AuthService(@Value("${tgb.service.auth.url}") String authUrl) {
        this.webClient = WebClient.builder().baseUrl(authUrl).build();
    }

    public boolean isUsernameFree(String username) throws AuthException {
        ApiResponse<List<String>> response = webClient.get().retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<String>>>() {})
                .block();
        if (Objects.isNull(response)) {
            throw new NoResponseException("No response.");
        }
        if (response.isSuccess()) {
            return !response.getData().contains(username);
        }
        ApiResponse.Error error = response.getError();
        throw new AuthException("Код ошибки: " + error.getCode() + ". Сообщение: " + error.getMessage());
    }

    public void register(String username, String password) throws AuthException {
        ApiResponse<String> response = webClient.post()
                .uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new RegisterRequest(username, password))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<String>>() {})
                .block();
        if (Objects.isNull(response)) {
            throw new NoResponseException("No response.");
        }
        if (!response.isSuccess()) {
            ApiResponse.Error error = response.getError();
            throw new AuthException("Код ошибки: " + error.getCode() + ". Сообщение: " + error.getMessage());
        }
    }

    public record RegisterRequest (String username, String password) {
    }
}
