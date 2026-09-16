package com.internal.internal_rag_core_service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * One turn (user question or assistant answer) within a {@link ChatConversation}.
 */
@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID conversationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageRole role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Convert(converter = StringListConverter.class)
    @Column(name = "source_documents")
    private List<String> sourceDocuments;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public static ChatMessage userMessage(UUID conversationId, String content) {
        return create(conversationId, MessageRole.USER, content, List.of());
    }

    public static ChatMessage assistantMessage(UUID conversationId, String content, List<String> sourceDocuments) {
        return create(conversationId, MessageRole.ASSISTANT, content, sourceDocuments);
    }

    private static ChatMessage create(UUID conversationId, MessageRole role, String content, List<String> sources) {
        ChatMessage message = new ChatMessage();
        message.id = UUID.randomUUID();
        message.conversationId = conversationId;
        message.role = role;
        message.content = content;
        message.sourceDocuments = sources;
        message.createdAt = Instant.now();
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMessage other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
