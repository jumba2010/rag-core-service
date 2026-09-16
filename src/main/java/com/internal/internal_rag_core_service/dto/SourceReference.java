package com.internal.internal_rag_core_service.dto;

public record SourceReference(
        String documentId,
        String filename,
        String snippet,
        Double score
) {
}
