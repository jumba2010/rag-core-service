package com.internal.internal_rag_core_service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Metadata record for a source document that has been (or is being) ingested
 * into the vector store. The actual embedded chunks live in the {@code vector_store}
 * table managed by Spring AI; rows here are what the API and UI list/search over.
 */
@Entity
@Table(name = "ingested_documents")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IngestedDocument {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(nullable = false)
    private int chunkCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentStatus status;

    @Column(length = 2000)
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant readyAt;

    public static IngestedDocument pending(String filename, String contentType, long sizeBytes) {
        IngestedDocument document = new IngestedDocument();
        document.id = UUID.randomUUID();
        document.filename = filename;
        document.contentType = contentType;
        document.sizeBytes = sizeBytes;
        document.status = DocumentStatus.PENDING;
        document.chunkCount = 0;
        document.createdAt = Instant.now();
        return document;
    }

    public void markProcessing() {
        this.status = DocumentStatus.PROCESSING;
    }

    public void markReady(int chunkCount) {
        this.status = DocumentStatus.READY;
        this.chunkCount = chunkCount;
        this.readyAt = Instant.now();
    }

    public void markFailed(String reason) {
        this.status = DocumentStatus.FAILED;
        this.failureReason = reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IngestedDocument other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
