package com.internal.internal_rag_core_service.repository;

import com.internal.internal_rag_core_service.domain.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, UUID> {
}
