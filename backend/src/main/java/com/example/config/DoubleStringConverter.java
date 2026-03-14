package com.example.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DoubleStringConverter implements AttributeConverter<Double, String> {

    @Override
    public String convertToDatabaseColumn(Double attribute) {
        return (attribute == null) ? null : String.valueOf(attribute);
    }

    @Override
    public Double convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return Double.valueOf(dbData);
    }
}