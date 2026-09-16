package com.internal.internal_rag_core_service.ingestion;

import com.internal.internal_rag_core_service.exception.DocumentProcessingException;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StandardDocumentIngestionPipeline extends DocumentIngestionPipeline {

    private final DocumentReaderFactory readerFactory;
    private final ChunkingStrategy chunkingStrategy;
    private final VectorStore vectorStore;

    public StandardDocumentIngestionPipeline(
            DocumentReaderFactory readerFactory,
            ChunkingStrategy chunkingStrategy,
            VectorStore vectorStore) {
        this.readerFactory = readerFactory;
        this.chunkingStrategy = chunkingStrategy;
        this.vectorStore = vectorStore;
    }

    @Override
    protected List<Document> parse(String filename, Resource resource) {
        try {
            DocumentReader reader = readerFactory.readerFor(filename, resource);
            List<Document> parsed = reader.get();
            if (parsed.isEmpty()) {
                throw new DocumentProcessingException("No extractable text found in '" + filename + "'");
            }
            return parsed;
        } catch (DocumentProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new DocumentProcessingException("Failed to parse '" + filename + "': " + e.getMessage(), e);
        }
    }

    @Override
    protected List<Document> chunk(List<Document> documents) {
        List<Document> chunks = chunkingStrategy.chunk(documents);
        if (chunks.isEmpty()) {
            throw new DocumentProcessingException("Document produced no chunks after splitting");
        }
        return chunks;
    }

    @Override
    protected void store(List<Document> chunks) {
        try {
            vectorStore.add(chunks);
        } catch (Exception e) {
            throw new DocumentProcessingException("Failed to store embeddings: " + e.getMessage(), e);
        }
    }
}
