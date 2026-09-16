package com.internal.internal_rag_core_service.ingestion;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.ByteArrayResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultDocumentReaderFactoryTest {

    private final DefaultDocumentReaderFactory factory = new DefaultDocumentReaderFactory();

    @Test
    void picksPdfReaderForPdfExtension() throws IOException {
        assertThat(factory.readerFor("policy.PDF", minimalPdf())).isInstanceOf(PagePdfDocumentReader.class);
    }

    @Test
    void fallsBackToTikaForOtherExtensions() {
        ByteArrayResource resource = new ByteArrayResource("irrelevant".getBytes());
        assertThat(factory.readerFor("notes.docx", resource)).isInstanceOf(TikaDocumentReader.class);
        assertThat(factory.readerFor("readme.txt", resource)).isInstanceOf(TikaDocumentReader.class);
        assertThat(factory.readerFor(null, resource)).isInstanceOf(TikaDocumentReader.class);
    }

    private ByteArrayResource minimalPdf() throws IOException {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            document.save(out);
            return new ByteArrayResource(out.toByteArray());
        }
    }
}
