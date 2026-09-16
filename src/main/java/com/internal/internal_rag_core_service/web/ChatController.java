package com.internal.internal_rag_core_service.web;

import com.internal.internal_rag_core_service.chat.RagChatService;
import com.internal.internal_rag_core_service.dto.ChatMessageResponse;
import com.internal.internal_rag_core_service.dto.ChatRequest;
import com.internal.internal_rag_core_service.dto.ChatResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "Chat", description = "Retrieval-augmented question answering over the ingested knowledge base")
public class ChatController {

    private final RagChatService ragChatService;

    public ChatController(RagChatService ragChatService) {
        this.ragChatService = ragChatService;
    }

    @PostMapping
    public ChatResponse ask(@Valid @RequestBody ChatRequest request) {
        return ragChatService.ask(request);
    }

    @GetMapping("/{conversationId}/messages")
    public List<ChatMessageResponse> history(@PathVariable UUID conversationId) {
        return ragChatService.history(conversationId).stream().map(ChatMessageResponse::from).toList();
    }
}
