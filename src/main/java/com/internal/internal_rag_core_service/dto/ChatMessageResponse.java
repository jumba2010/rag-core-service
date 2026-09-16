package com.internal.internal_rag_core_service.dto;

import com.internal.internal_rag_core_service.domain.ChatMessage;
import com.internal.internal_rag_core_service.domain.MessageRole;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChatMessageResponse(
        UUID id,
        MessageRole role,
        String content,
        List<String> sourceDocuments,
        Instant createdAt
) {

    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getSourceDocuments(),
                message.getCreatedAt()
        );
    }
}
