package com.internal.internal_rag_core_service.ingestion;

import com.internal.internal_rag_core_service.domain.IngestedDocument;
import com.internal.internal_rag_core_service.exception.DocumentNotFoundException;
import com.internal.internal_rag_core_service.exception.DocumentProcessingException;
import com.internal.internal_rag_core_service.repository.IngestedDocumentRepository;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentIngestionServiceImpl implements DocumentIngestionService {

    private final IngestedDocumentRepository documentRepository;
    private final DocumentProcessingWorker worker;
    private final VectorStore vectorStore;

    public DocumentIngestionServiceImpl(
            IngestedDocumentRepository documentRepository,
            DocumentProcessingWorker worker,
            VectorStore vectorStore) {
        this.documentRepository = documentRepository;
        this.worker = worker;
        this.vectorStore = vectorStore;
    }

    @Override
    public IngestedDocument ingest(MultipartFile file) {
        if (file.isEmpty()) {
            throw new DocumentProcessingException("Uploaded file is empty");
        }

        String filename = originalFilenameOf(file);
        IngestedDocument document = IngestedDocument.pending(filename, file.getContentType(), file.getSize());
        documentRepository.save(document);

        byte[] content = readBytes(file);
        worker.process(document.getId(), filename, content);

        return document;
    }

    @Override
    public List<IngestedDocument> listAll() {
        return documentRepository.findAll();
    }

    @Override
    public IngestedDocument get(UUID documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
    }

    @Override
    public void delete(UUID documentId) {
        IngestedDocument document = get(documentId);
        var filter = new FilterExpressionBuilder().eq("document_id", documentId.toString()).build();
        vectorStore.delete(filter);
        documentRepository.delete(document);
    }

    private String originalFilenameOf(MultipartFile file) {
        String filename = file.getOriginalFilename();
        return (filename == null || filename.isBlank()) ? "untitled" : filename;
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new DocumentProcessingException("Could not read uploaded file: " + e.getMessage(), e);
        }
    }
}
