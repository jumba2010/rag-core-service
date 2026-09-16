package com.internal.internal_rag_core_service.ingestion;

import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Chooses {@link PagePdfDocumentReader} for PDFs (it gives us per-page metadata,
 * useful for citations) and falls back to {@link TikaDocumentReader} - backed by
 * Apache Tika's format auto-detection - for everything else: Word, PowerPoint,
 * HTML, RTF, plain text, Markdown, CSV, and so on.
 */
@Component
public class DefaultDocumentReaderFactory implements DocumentReaderFactory {

    @Override
    public DocumentReader readerFor(String filename, Resource resource) {
        return switch (extensionOf(filename)) {
            case "pdf" -> new PagePdfDocumentReader(resource);
            default -> new TikaDocumentReader(resource);
        };
    }

    private String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        return lastDot < 0 ? "" : filename.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }
}
