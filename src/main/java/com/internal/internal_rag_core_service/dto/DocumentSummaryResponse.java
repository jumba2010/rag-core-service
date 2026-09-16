package com.internal.internal_rag_core_service.dto;

import com.internal.internal_rag_core_service.domain.DocumentStatus;
import com.internal.internal_rag_core_service.domain.IngestedDocument;

import java.time.Instant;
import java.util.UUID;

public record DocumentSummaryResponse(
        UUID id,
        String filename,
        String contentType,
        long sizeBytes,
        int chunkCount,
        DocumentStatus status,
        String failureReason,
        Instant createdAt,
        Instant readyAt
) {

    public static DocumentSummaryResponse from(IngestedDocument document) {
        return new DocumentSummaryResponse(
                document.getId(),
                document.getFilename(),
                document.getContentType(),
                document.getSizeBytes(),
                document.getChunkCount(),
                document.getStatus(),
                document.getFailureReason(),
                document.getCreatedAt(),
                document.getReadyAt()
        );
    }
}
