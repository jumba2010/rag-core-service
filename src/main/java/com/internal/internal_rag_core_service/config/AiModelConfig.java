package com.internal.internal_rag_core_service.config;

import com.internal.internal_rag_core_service.chat.ChatModelLoggingDecorator;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiModelConfig {

    private static final String SYSTEM_PROMPT = """
            You are the internal knowledge assistant for this organization.
            Answer the user's question using ONLY the information given to you
            as retrieved context. If the context does not contain the answer,
            say plainly that you don't have that information in the knowledge
            base - do not guess or use outside knowledge. Keep answers concise
            and cite the source filename(s) you relied on when relevant.
            """;

    /**
     * Wraps the chat {@link ChatModel} autoconfigured by spring-ai-starter-model-openai
     * (pointed at xAI's OpenAI-compatible endpoint - see application.yml) with logging.
     * Marked {@link Primary} so both this bean and the auto-configured
     * {@code ChatClient.Builder} resolve to the decorated instance.
     */
    @Bean
    @Primary
    public ChatModel loggingChatModel(ChatModel chatModel) {
        return new ChatModelLoggingDecorator(chatModel);
    }

    @Bean
    public ChatClient ragChatClient(
            ChatClient.Builder chatClientBuilder,
            VectorStore vectorStore,
            @Value("${app.rag.top-k:5}") int topK,
            @Value("${app.rag.similarity-threshold:0.5}") double similarityThreshold) {

        QuestionAnswerAdvisor retrievalAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .topK(topK)
                        .similarityThreshold(similarityThreshold)
                        .build())
                .build();

        return chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(retrievalAdvisor)
                .build();
    }
}
