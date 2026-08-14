package com.ohgiraffer.consultation.infrastructure.converter;

import com.ohgiraffer.global.crypto.AesColumnEncryptor;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;

@Converter
public class ConsultationNoteConverter implements AttributeConverter<String, String> {

    @Autowired
    private AesColumnEncryptor aesColumnEncryptor;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return aesColumnEncryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return aesColumnEncryptor.decrypt(dbData);
    }
}