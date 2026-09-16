package com.internal.internal_rag_core_service.ingestion;

import org.springframework.ai.document.DocumentReader;
import org.springframework.core.io.Resource;

/**
 * Factory that picks the right Spring AI {@link DocumentReader} implementation
 * for a given file, based on its name/extension. Isolates the controller and
 * ingestion service from having to know which parser handles which format.
 */
public interface DocumentReaderFactory {

    DocumentReader readerFor(String filename, Resource resource);
}
