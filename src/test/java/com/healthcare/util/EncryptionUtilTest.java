package com.healthcare.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class EncryptionUtilTest {

    @Autowired
    private EncryptionUtil encryptionUtil;

    @Test
    void testEncryptDecrypt_Success() {
        // Given
        String originalText = "Sensitive Patient Information";

        // When
        String encrypted = encryptionUtil.encrypt(originalText);
        String decrypted = encryptionUtil.decrypt(encrypted);

        // Then
        assertNotNull(encrypted);
        assertNotEquals(originalText, encrypted);
        assertEquals(originalText, decrypted);
    }

    @Test
    void testEncryptDecrypt_EmptyString() {
        // Given
        String originalText = "";

        // When
        String encrypted = encryptionUtil.encrypt(originalText);
        String decrypted = encryptionUtil.decrypt(encrypted);

        // Then
        assertEquals(originalText, decrypted);
    }

    @Test
    void testEncryptDecrypt_SpecialCharacters() {
        // Given
        String originalText = "Patient: John Doe, DOB: 01/01/1990, SSN: 123-45-6789";

        // When
        String encrypted = encryptionUtil.encrypt(originalText);
        String decrypted = encryptionUtil.decrypt(encrypted);

        // Then
        assertEquals(originalText, decrypted);
    }

    @Test
    void testEncrypt_SameInputDifferentOutput() {
        // Given
        String originalText = "Test Data";

        // When - Encrypt same text twice
        String encrypted1 = encryptionUtil.encrypt(originalText);
        String encrypted2 = encryptionUtil.encrypt(originalText);

        // Then - Should be different due to random IV
        assertNotEquals(encrypted1, encrypted2);
        assertEquals(originalText, encryptionUtil.decrypt(encrypted1));
        assertEquals(originalText, encryptionUtil.decrypt(encrypted2));
    }

    @Test
    void testDecrypt_InvalidData_ThrowsException() {
        // Given
        String invalidEncryptedData = "invalid_encrypted_data";

        // When & Then
        assertThrows(Exception.class, () -> {
            encryptionUtil.decrypt(invalidEncryptedData);
        });
    }
}
