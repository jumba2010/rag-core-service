package com.internal.internal_rag_core_service.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StringListConverterTest {

    private final StringListConverter converter = new StringListConverter();

    @Test
    void roundTripsMultipleValues() {
        List<String> original = List.of("policy.pdf", "handbook.docx", "faq.md");

        String column = converter.convertToDatabaseColumn(original);
        List<String> restored = converter.convertToEntityAttribute(column);

        assertThat(restored).containsExactlyElementsOf(original);
    }

    @Test
    void treatsNullAndEmptyAsEmptyList() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToDatabaseColumn(List.of())).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isEmpty();
        assertThat(converter.convertToEntityAttribute("")).isEmpty();
    }
}
