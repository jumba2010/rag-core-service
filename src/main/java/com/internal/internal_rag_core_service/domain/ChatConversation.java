package com.internal.internal_rag_core_service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A single conversation thread between a frontend client and the RAG assistant.
 * Groups an ordered sequence of {@link ChatMessage} turns.
 */
@Entity
@Table(name = "chat_conversations")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatConversation {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant lastMessageAt;

    public static ChatConversation startingWith(String firstUserMessage) {
        return startingWith(UUID.randomUUID(), firstUserMessage);
    }

    /**
     * Starts a conversation under a caller-supplied id rather than a
     * server-generated one. Some frontends (e.g. a chat widget that
     * pre-generates an id before the first message) need to pick their own
     * conversation id up front; letting them own it here is simpler than
     * requiring a round trip just to learn the real id before asking anything.
     */
    public static ChatConversation startingWith(UUID id, String firstUserMessage) {
        ChatConversation conversation = new ChatConversation();
        conversation.id = id;
        conversation.title = shortenToTitle(firstUserMessage);
        conversation.createdAt = Instant.now();
        conversation.lastMessageAt = conversation.createdAt;
        return conversation;
    }

    public void touch() {
        this.lastMessageAt = Instant.now();
    }

    private static String shortenToTitle(String message) {
        String trimmed = message.strip();
        return trimmed.length() <= 80 ? trimmed : trimmed.substring(0, 77) + "...";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatConversation other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
