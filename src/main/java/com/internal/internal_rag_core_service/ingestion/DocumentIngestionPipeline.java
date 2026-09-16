package com.internal.internal_rag_core_service.ingestion;

import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.UUID;

/**
 * Template Method skeleton for turning one uploaded file into embedded, stored
 * vector chunks. The four-step order (parse -&gt; tag -&gt; chunk -&gt; store) is fixed
 * and final; subclasses only supply how each step is carried out. This leaves
 * room for future variants - e.g. a pipeline that OCRs scanned PDFs before
 * parsing, or redacts PII before storage - without branching the orchestration
 * logic itself.
 */
public abstract class DocumentIngestionPipeline {

    public final List<Document> run(UUID documentId, String filename, Resource resource) {
        List<Document> parsed = parse(filename, resource);
        List<Document> tagged = tag(parsed, documentId, filename);
        List<Document> chunks = chunk(tagged);
        store(chunks);
        return chunks;
    }

    protected abstract List<Document> parse(String filename, Resource resource);

    protected abstract List<Document> chunk(List<Document> documents);

    protected abstract void store(List<Document> chunks);

    protected List<Document> tag(List<Document> documents, UUID documentId, String filename) {
        return documents.stream()
                .map(document -> document.mutate()
                        .metadata("document_id", documentId.toString())
                        .metadata("filename", filename)
                        .build())
                .toList();
    }
}
