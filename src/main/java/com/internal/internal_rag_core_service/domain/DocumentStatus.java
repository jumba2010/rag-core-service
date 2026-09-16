package com.internal.internal_rag_core_service.domain;

/**
 * Lifecycle of a document as it moves through the ingestion pipeline.
 */
public enum DocumentStatus {
    PENDING,
    PROCESSING,
    READY,
    FAILED
}
