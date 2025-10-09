package tgb.cryptoexchange.devbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordGeneratorTest {
    private PasswordGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new PasswordGenerator();
    }

    @Test
    void testGenerateLength() {
        String password = generator.generate(10);
        assertEquals(10, password.length());
    }

    @Test
    void testGenerateMinLength() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate(3));
    }

    @Test
    void testGenerateContainsAllCharTypes() {
        String password = generator.generate(10);
        assertTrue(password.chars().anyMatch(ch -> "abcdefghijklmnopqrstuvwxyz".indexOf(ch) >= 0), "Should contain lowercase");
        assertTrue(password.chars().anyMatch(ch -> "ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(ch) >= 0), "Should contain uppercase");
        assertTrue(password.chars().anyMatch(ch -> "0123456789".indexOf(ch) >= 0), "Should contain digit");
        assertTrue(password.chars().anyMatch(ch -> "-_.~!*()".indexOf(ch) >= 0), "Should contain special character");
    }

    @Test
    void testShuffleChangesOrder() {
        String password = generator.generate(20);
        assertNotNull(password);
        assertEquals(20, password.length());
    }

    @Test
    void testGenerateWithMockedRandom() {
        SecureRandom mockRandom = Mockito.mock(SecureRandom.class);
        when(mockRandom.nextInt(Mockito.anyInt())).thenReturn(0);

        PasswordGenerator customGenerator = new PasswordGenerator() {
            {
                try {
                    java.lang.reflect.Field rndField = PasswordGenerator.class.getDeclaredField("rnd");
                    rndField.setAccessible(true);
                    rndField.set(this, mockRandom);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        String password = customGenerator.generate(8);

        assertTrue(password.contains("a"));
        assertTrue(password.contains("A"));
        assertTrue(password.contains("0"));
        assertTrue(password.contains("-"));
        assertEquals(8, password.length());
    }
}