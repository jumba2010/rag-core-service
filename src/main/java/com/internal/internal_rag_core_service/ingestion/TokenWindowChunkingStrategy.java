package com.internal.internal_rag_core_service.ingestion;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Default {@link ChunkingStrategy}: splits on token-count windows (via jtokkit)
 * so each chunk fits comfortably inside the embedding model's context window
 * regardless of how dense the source text is.
 */
@Component
public class TokenWindowChunkingStrategy implements ChunkingStrategy {

    private final TokenTextSplitter splitter;

    public TokenWindowChunkingStrategy(
            @Value("${app.ingestion.chunk-size-tokens:800}") int chunkSizeTokens,
            @Value("${app.ingestion.min-chunk-size-chars:350}") int minChunkSizeChars) {
        this.splitter = TokenTextSplitter.builder()
                .withChunkSize(chunkSizeTokens)
                .withMinChunkSizeChars(minChunkSizeChars)
                .withKeepSeparator(true)
                .build();
    }

    @Override
    public List<Document> chunk(List<Document> documents) {
        return splitter.split(documents);
    }
}
