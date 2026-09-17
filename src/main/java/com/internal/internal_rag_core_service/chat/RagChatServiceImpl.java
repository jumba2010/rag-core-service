package com.internal.internal_rag_core_service.chat;

import com.internal.internal_rag_core_service.domain.ChatConversation;
import com.internal.internal_rag_core_service.domain.ChatMessage;
import com.internal.internal_rag_core_service.domain.MessageRole;
import com.internal.internal_rag_core_service.dto.ChatRequest;
import com.internal.internal_rag_core_service.dto.ChatResponse;
import com.internal.internal_rag_core_service.dto.SourceReference;
import com.internal.internal_rag_core_service.exception.ChatGenerationException;
import com.internal.internal_rag_core_service.exception.ConversationNotFoundException;
import com.internal.internal_rag_core_service.repository.ChatConversationRepository;
import com.internal.internal_rag_core_service.repository.ChatMessageRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RagChatServiceImpl implements RagChatService {

    private final ChatClient ragChatClient;
    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;

    public RagChatServiceImpl(
            ChatClient ragChatClient,
            ChatConversationRepository conversationRepository,
            ChatMessageRepository messageRepository) {
        this.ragChatClient = ragChatClient;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional
    public ChatResponse ask(ChatRequest request) {
        ChatConversation conversation = resolveConversation(request);
        List<Message> history = priorMessagesFor(conversation.getId());

        messageRepository.save(ChatMessage.userMessage(conversation.getId(), request.message()));

        ChatClientResponse clientResponse = callModel(request.message(), history);
        String answer = clientResponse.chatResponse().getResult().getOutput().getText();
        List<SourceReference> sources = extractSources(clientResponse);

        messageRepository.save(ChatMessage.assistantMessage(
                conversation.getId(), answer, sources.stream().map(SourceReference::filename).distinct().toList()));

        conversation.touch();
        conversationRepository.save(conversation);

        return new ChatResponse(conversation.getId(), answer, sources);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> history(UUID conversationId) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new ConversationNotFoundException(conversationId);
        }
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    /**
     * Get-or-create rather than a strict lookup: some frontends pre-generate a
     * conversation id client-side before the very first message, so a
     * client-supplied id that doesn't exist yet just starts a new
     * conversation under that id instead of failing the request.
     */
    private ChatConversation resolveConversation(ChatRequest request) {
        if (request.conversationId() == null) {
            return conversationRepository.save(ChatConversation.startingWith(request.message()));
        }
        return conversationRepository.findById(request.conversationId())
                .orElseGet(() -> conversationRepository.save(
                        ChatConversation.startingWith(request.conversationId(), request.message())));
    }

    private List<Message> priorMessagesFor(UUID conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .<Message>map(m -> m.getRole() == MessageRole.USER
                        ? new UserMessage(m.getContent())
                        : new AssistantMessage(m.getContent()))
                .toList();
    }

    private ChatClientResponse callModel(String question, List<Message> history) {
        try {
            return ragChatClient.prompt()
                    .messages(history)
                    .user(question)
                    .call()
                    .chatClientResponse();
        } catch (Exception e) {
            throw new ChatGenerationException("Chat model call failed", e);
        }
    }

    private List<SourceReference> extractSources(ChatClientResponse response) {
        Object retrieved = response.context().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
        if (!(retrieved instanceof List<?> documents)) {
            return List.of();
        }

        // Keep only the first (highest-scoring) chunk per source document.
        Map<String, SourceReference> byFilename = new LinkedHashMap<>();
        for (Object item : documents) {
            Document document = (Document) item;
            String filename = String.valueOf(document.getMetadata().getOrDefault("filename", "unknown"));
            byFilename.putIfAbsent(filename, new SourceReference(
                    String.valueOf(document.getMetadata().get("document_id")),
                    filename,
                    snippetOf(document.getText()),
                    document.getScore()));
        }
        return List.copyOf(byFilename.values());
    }

    private String snippetOf(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.strip();
        return trimmed.length() <= 240 ? trimmed : trimmed.substring(0, 237) + "...";
    }
}
