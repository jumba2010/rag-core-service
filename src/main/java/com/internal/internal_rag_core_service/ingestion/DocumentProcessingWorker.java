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
            var chunks = pipeline.run(documentId, filename, namedResource(filename, content));
            document.markReady(chunks.size());
            log.info("Ingested document {} ('{}') into {} chunks", documentId, filename, chunks.size());
        } catch (Throwable t) {
            // Catching Throwable (not just Exception) is deliberate: a bad
            // classpath in a parsing library can surface as a LinkageError
            // (e.g. NoSuchMethodError) rather than a checked exception. Left
            // uncaught, the document would stay stuck in PROCESSING forever
            // instead of being reported as FAILED.
            log.warn("Ingestion failed for document {} ('{}')", documentId, filename, t);
            document.markFailed(t.getMessage());
        } finally {
            documentRepository.save(document);
        }
    }

    /**
     * Plain {@link ByteArrayResource} always returns null from getFilename(),
     * which PagePdfDocumentReader stores verbatim as PDF page metadata -
     * violating Spring AI's Document invariant that no metadata value may be
     * null. Carrying the real filename through avoids that.
     */
    private ByteArrayResource namedResource(String filename, byte[] content) {
        return new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }
}
