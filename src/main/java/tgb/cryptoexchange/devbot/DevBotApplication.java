package tgb.cryptoexchange.devbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"tgb.cryptoexchange", "org.telegram"})
public class DevBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevBotApplication.class, args);
    }

}
