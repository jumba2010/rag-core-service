package com.internal.internal_rag_core_service.web;

import com.internal.internal_rag_core_service.dto.DocumentSummaryResponse;
import com.internal.internal_rag_core_service.ingestion.DocumentIngestionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Documents", description = "Upload and manage the knowledge base ingested into the vector store")
public class DocumentController {

    private final DocumentIngestionService ingestionService;

    public DocumentController(DocumentIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping
    public ResponseEntity<DocumentSummaryResponse> upload(@RequestParam("file") MultipartFile file) {
        var document = ingestionService.ingest(file);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(DocumentSummaryResponse.from(document));
    }

    @GetMapping
    public List<DocumentSummaryResponse> listAll() {
        return ingestionService.listAll().stream().map(DocumentSummaryResponse::from).toList();
    }

    @GetMapping("/{documentId}")
    public DocumentSummaryResponse get(@PathVariable UUID documentId) {
        return DocumentSummaryResponse.from(ingestionService.get(documentId));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> delete(@PathVariable UUID documentId) {
        ingestionService.delete(documentId);
        return ResponseEntity.noContent().build();
    }
}
