package com.ohgiraffer.consultation.infrastructure.converter;

import com.ohgiraffer.global.crypto.AesColumnEncryptor;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Slf4j
@Component
@Converter
@RequiredArgsConstructor
public class ConsultationNoteConverter implements AttributeConverter<String, String> {

    private static final int MIN_ENCRYPTED_LENGTH = 24;

    private final AesColumnEncryptor aesColumnEncryptor;   // final + 생성자 주입

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return aesColumnEncryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        if (!looksEncrypted(dbData)) {
            log.warn("암호화되지 않은 상담 데이터가 감지되었습니다. 마이그레이션 배치가 필요합니다.");
            return dbData;
        }
        return aesColumnEncryptor.decrypt(dbData);
    }

    private boolean looksEncrypted(String value) {
        if (value.length() < MIN_ENCRYPTED_LENGTH) {
            return false;
        }
        try {
            Base64.getDecoder().decode(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}