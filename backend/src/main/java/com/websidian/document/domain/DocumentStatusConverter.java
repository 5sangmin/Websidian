package com.websidian.document.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DocumentStatusConverter implements AttributeConverter<DocumentStatus, String> {

    @Override
    public String convertToDatabaseColumn(DocumentStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase();
    }

    @Override
    public DocumentStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        return DocumentStatus.valueOf(dbData.toUpperCase());
    }
}