package tgb.cryptoexchange.devbot.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class PasswordGenerator {
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*";

    private static final String ALL = LOWER + UPPER + DIGITS + SPECIAL;

    private final SecureRandom rnd = new SecureRandom();

    /**
     * Генерирует пароль с гарантией соответствия regex.
     *
     * @param length длина пароля (минимум 4)
     * @return пароль, удовлетворяющий условиям
     */
    public String generate(int length) {
        if (length < 4) {
            throw new IllegalArgumentException("Минимальная длина пароля = 4");
        }

        StringBuilder pw = new StringBuilder(length);
        pw.append(randomChar(LOWER));
        pw.append(randomChar(UPPER));
        pw.append(randomChar(DIGITS));
        pw.append(randomChar(SPECIAL));

        for (int i = 4; i < length; i++) {
            pw.append(randomChar(ALL));
        }
        return shuffle(pw.toString());
    }


    private char randomChar(String alphabet) {
        return alphabet.charAt(rnd.nextInt(alphabet.length()));
    }


    private String shuffle(String input) {
        char[] arr = input.toCharArray();
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            char tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
        return new String(arr);
    }
}
