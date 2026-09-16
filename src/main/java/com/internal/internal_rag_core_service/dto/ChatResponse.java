package com.internal.internal_rag_core_service.dto;

import java.util.List;
import java.util.UUID;

public record ChatResponse(
        UUID conversationId,
        String answer,
        List<SourceReference> sources
) {
}
