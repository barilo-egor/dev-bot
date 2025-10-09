package tgb.cryptoexchange.devbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SpringConfig {

    @Bean
    public WebClient authWebClient(@Value("${tgb.service.auth.url}") String authUrl) {
        return WebClient.builder().baseUrl(authUrl).build();
    }
}
