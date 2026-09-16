package com.internal.internal_rag_core_service.exception;

/**
 * Thrown when the underlying chat model (Bedrock) fails to produce a response.
 */
public class ChatGenerationException extends RuntimeException {

    public ChatGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
