package com.internal.internal_rag_core_service.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;

/**
 * Persists a {@code List<String>} as a single delimited column so we avoid a
 * join table for what is, here, always a short, order-insignificant set of
 * source document names attached to an assistant reply. Uses the ASCII "unit
 * separator" control character (0x1F) as the delimiter since it cannot appear
 * in a filename.
 */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    private static final char UNIT_SEPARATOR = (char) 0x1F;
    private static final String DELIMITER = String.valueOf(UNIT_SEPARATOR);

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        return attribute == null || attribute.isEmpty() ? null : String.join(DELIMITER, attribute);
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        return dbData == null || dbData.isBlank() ? List.of() : Arrays.asList(dbData.split(DELIMITER));
    }
}
