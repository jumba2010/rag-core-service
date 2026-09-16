package com.internal.internal_rag_core_service.ingestion;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * Strategy for splitting parsed documents into embeddable chunks. Kept as an
 * explicit seam (rather than calling a {@code TextSplitter} directly from the
 * pipeline) so alternative chunking approaches - fixed character windows,
 * semantic/sentence-aware splitting, table-aware splitting - can be swapped in
 * per document type without touching {@link DocumentIngestionPipeline}.
 */
public interface ChunkingStrategy {

    List<Document> chunk(List<Document> documents);
}
