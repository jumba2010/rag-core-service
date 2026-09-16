package com.internal.internal_rag_core_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables {@code @Async} processing for document ingestion. The executor
 * itself is Spring Boot's auto-configured virtual-thread task executor
 * (activated by {@code spring.threads.virtual.enabled=true}), so each upload
 * is processed on a cheap virtual thread rather than a pooled platform thread.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
