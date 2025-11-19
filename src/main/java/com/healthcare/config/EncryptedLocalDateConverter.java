package com.healthcare.config;

import com.healthcare.util.EncryptionUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * JPA AttributeConverter for automatic encryption/decryption of LocalDate fields
 * Used for HIPAA-compliant encryption of sensitive dates (e.g., date of birth)
 *
 * Usage: @Convert(converter = EncryptedLocalDateConverter.class)
 */
@Converter
@Component
public class EncryptedLocalDateConverter implements AttributeConverter<LocalDate, String> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static EncryptionUtil encryptionUtil;

    @Autowired
    public void setEncryptionUtil(EncryptionUtil util) {
        EncryptedLocalDateConverter.encryptionUtil = util;
    }

    @Override
    public String convertToDatabaseColumn(LocalDate attribute) {
        if (attribute == null) {
            return null;
        }
        String dateString = attribute.format(FORMATTER);
        return encryptionUtil.encrypt(dateString);
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        String decryptedDate = encryptionUtil.decrypt(dbData);
        return LocalDate.parse(decryptedDate, FORMATTER);
    }
}
