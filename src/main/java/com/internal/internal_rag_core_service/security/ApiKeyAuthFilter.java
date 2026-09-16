package com.internal.internal_rag_core_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authenticates every request under {@code /api/**} (see {@code SecurityConfig},
 * which scopes this filter's URL pattern) by comparing an {@code X-API-Key}
 * header against the single shared secret configured for this service.
 * Deliberately simple: a plain servlet filter rather than pulling in Spring
 * Security, because this is an internal, machine-to-machine API sitting behind
 * one frontend, not a multi-tenant product with per-user authorization rules.
 * Swap for Spring Security + OAuth2/JWT if that changes.
 */
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-API-Key";

    private final String expectedApiKey;

    public ApiKeyAuthFilter(String expectedApiKey) {
        this.expectedApiKey = expectedApiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String providedKey = request.getHeader(HEADER_NAME);

        if (expectedApiKey != null && expectedApiKey.equals(providedKey)) {
            chain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write("""
                {"title":"Unauthorized","status":401,"detail":"Missing or invalid %s header"}
                """.formatted(HEADER_NAME));
    }
}
