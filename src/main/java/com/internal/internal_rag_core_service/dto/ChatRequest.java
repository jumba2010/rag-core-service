package com.internal.internal_rag_core_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * @param conversationId when null, a new conversation is started
 * @param message the user's question
 */
public record ChatRequest(

        UUID conversationId,

        @NotBlank(message = "message must not be blank")
        @Size(max = 8000, message = "message must be at most 8000 characters")
        String message
) {
}
