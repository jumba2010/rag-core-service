package com.internal.internal_rag_core_service.ingestion;

import com.internal.internal_rag_core_service.domain.IngestedDocument;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Facade the web layer talks to for everything document-related: uploading,
 * listing, and removing ingested documents. Hides the fact that "removing a
 * document" really means deleting both a JPA row and a set of vector store
 * chunks, and that "uploading" means parse + chunk + embed + persist status.
 */
public interface DocumentIngestionService {

    IngestedDocument ingest(MultipartFile file);

    List<IngestedDocument> listAll();

    IngestedDocument get(UUID documentId);

    void delete(UUID documentId);
}
