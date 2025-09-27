package tgb.cryptoexchange.devbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "bot")
@Data
public class DevBotConfig {

    private String username;

    private String token;

    private List<Long> adminsChatIds;
}
