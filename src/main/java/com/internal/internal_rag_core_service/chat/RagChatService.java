package com.internal.internal_rag_core_service.chat;

import com.internal.internal_rag_core_service.domain.ChatMessage;
import com.internal.internal_rag_core_service.dto.ChatRequest;
import com.internal.internal_rag_core_service.dto.ChatResponse;

import java.util.List;
import java.util.UUID;

/**
 * Facade over retrieval-augmented generation: given a user question (and
 * optionally an existing conversation to continue), retrieves relevant
 * document chunks, asks the chat model to answer grounded in them, and
 * persists both turns.
 */
public interface RagChatService {

    ChatResponse ask(ChatRequest request);

    List<ChatMessage> history(UUID conversationId);
}
