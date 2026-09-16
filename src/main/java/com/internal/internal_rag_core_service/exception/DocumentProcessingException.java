package com.internal.internal_rag_core_service.exception;

/**
 * Thrown when a document cannot be parsed, chunked, or embedded during ingestion.
 */
public class DocumentProcessingException extends RuntimeException {

    public DocumentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public DocumentProcessingException(String message) {
        super(message);
    }
}
