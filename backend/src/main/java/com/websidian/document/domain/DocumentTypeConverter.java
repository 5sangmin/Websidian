package com.websidian.document.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DocumentTypeConverter implements AttributeConverter<DocumentType, String> {

    @Override
    public String convertToDatabaseColumn(DocumentType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase();
    }

    @Override
    public DocumentType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        return DocumentType.valueOf(dbData.toUpperCase());
    }
}