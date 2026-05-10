package com.websidian.document.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class VisibilityConverter implements AttributeConverter<Visibility, String> {

    @Override
    public String convertToDatabaseColumn(Visibility attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase();
    }

    @Override
    public Visibility convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        return Visibility.valueOf(dbData.toUpperCase());
    }
}