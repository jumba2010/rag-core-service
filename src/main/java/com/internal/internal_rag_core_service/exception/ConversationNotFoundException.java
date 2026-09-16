package com.internal.internal_rag_core_service.exception;

import java.util.UUID;

public class ConversationNotFoundException extends RuntimeException {

    public ConversationNotFoundException(UUID conversationId) {
        super("No chat conversation found with id " + conversationId);
    }
}
