package com.internal.internal_rag_core_service.exception;

import java.util.UUID;

public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(UUID documentId) {
        super("No ingested document found with id " + documentId);
    }
}
