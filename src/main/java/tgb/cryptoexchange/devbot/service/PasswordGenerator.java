package tgb.cryptoexchange.devbot.service;

import org.springframework.stereotype.Service;

@Service
public class PasswordGenerator {

    public String generate() {
        byte[] bytes = new byte[32];
        new java.security.SecureRandom().nextBytes(bytes);
        return java.util.Base64.getEncoder().encodeToString(bytes);
    }
}
