package com.internal.internal_rag_core_service.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

/**
 * Decorator around the Bedrock-backed {@link ChatModel} that adds latency and
 * token-usage logging without the rest of the application (or Spring AI's own
 * advisor chain) knowing it's there. Wrapping - rather than editing the
 * autoconfigured bean - keeps this cross-cutting concern separate from model
 * selection/config and easy to remove.
 */
public class ChatModelLoggingDecorator implements ChatModel {

    private static final Logger log = LoggerFactory.getLogger(ChatModelLoggingDecorator.class);

    private final ChatModel delegate;

    public ChatModelLoggingDecorator(ChatModel delegate) {
        this.delegate = delegate;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        long startedAt = System.nanoTime();
        try {
            ChatResponse response = delegate.call(prompt);
            logCompletion(startedAt, response);
            return response;
        } catch (RuntimeException e) {
            log.error("Bedrock chat call failed after {} ms", elapsedMillis(startedAt), e);
            throw e;
        }
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        long startedAt = System.nanoTime();
        return delegate.stream(prompt)
                .doOnComplete(() -> log.debug("Bedrock chat stream completed in {} ms", elapsedMillis(startedAt)))
                .doOnError(e -> log.error("Bedrock chat stream failed after {} ms", elapsedMillis(startedAt), e));
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return delegate.getDefaultOptions();
    }

    private void logCompletion(long startedAt, ChatResponse response) {
        var usage = response.getMetadata() != null ? response.getMetadata().getUsage() : null;
        if (usage != null) {
            log.info("Bedrock chat call completed in {} ms (promptTokens={}, completionTokens={})",
                    elapsedMillis(startedAt), usage.getPromptTokens(), usage.getCompletionTokens());
        } else {
            log.info("Bedrock chat call completed in {} ms", elapsedMillis(startedAt));
        }
    }

    private long elapsedMillis(long startedAtNanos) {
        return (System.nanoTime() - startedAtNanos) / 1_000_000;
    }
}
