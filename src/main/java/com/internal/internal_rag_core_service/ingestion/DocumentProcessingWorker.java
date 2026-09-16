package com.internal.internal_rag_core_service.ingestion;

import com.internal.internal_rag_core_service.domain.IngestedDocument;
import com.internal.internal_rag_core_service.exception.DocumentNotFoundException;
import com.internal.internal_rag_core_service.repository.IngestedDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Runs the ingestion pipeline off the request thread. Deliberately a separate
 * bean (rather than a private method on {@link DocumentIngestionServiceImpl})
 * because Spring's {@code @Async} proxy cannot intercept self-invoked calls.
 */
@Component
public class DocumentProcessingWorker {

    private static final Logger log = LoggerFactory.getLogger(DocumentProcessingWorker.class);

    private final IngestedDocumentRepository documentRepository;
    private final DocumentIngestionPipeline pipeline;

    public DocumentProcessingWorker(IngestedDocumentRepository documentRepository, DocumentIngestionPipeline pipeline) {
        this.documentRepository = documentRepository;
        this.pipeline = pipeline;
    }

    @Async
    public void process(UUID documentId, String filename, byte[] content) {
        IngestedDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
        document.markProcessing();
        documentRepository.save(document);

        try {
            var chunks = pipeline.run(documentId, filename, new ByteArrayResource(content));
            document.markReady(chunks.size());
            log.info("Ingested document {} ('{}') into {} chunks", documentId, filename, chunks.size());
        } catch (Exception e) {
            log.warn("Ingestion failed for document {} ('{}')", documentId, filename, e);
            document.markFailed(e.getMessage());
        } finally {
            documentRepository.save(document);
        }
    }
}
