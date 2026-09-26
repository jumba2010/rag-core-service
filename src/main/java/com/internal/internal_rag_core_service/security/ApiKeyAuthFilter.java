package com.internal.internal_rag_core_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Authenticates every request under {@code /api/**} (see {@code SecurityConfig},
 * which scopes this filter's URL pattern) by comparing an {@code X-API-Key}
 * header against the single shared secret configured for this service.
 * Deliberately simple: a plain servlet filter rather than pulling in Spring
 * Security, because this is an internal, machine-to-machine API sitting behind
 * one frontend, not a multi-tenant product with per-user authorization rules.
 * Swap for Spring Security + OAuth2/JWT if that changes.
 *
 * <p>Two properties matter for a shared-secret check like this:
 * <ul>
 *   <li><b>Constant-time comparison</b> - {@link MessageDigest#isEqual} is used
 *   instead of {@link String#equals} so response timing does not leak how many
 *   leading characters of a guessed key were correct.</li>
 *   <li><b>Fail closed</b> - if no key is configured (e.g. {@code API_KEY} left
 *   unset, which resolves to an empty string), every request is rejected rather
 *   than accepting a blank {@code X-API-Key} header.</li>
 * </ul>
 */
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-API-Key";

    private final byte[] expectedApiKey;

    public ApiKeyAuthFilter(String expectedApiKey) {
        this.expectedApiKey = (expectedApiKey == null || expectedApiKey.isBlank())
                ? null
                : expectedApiKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String providedKey = request.getHeader(HEADER_NAME);

        if (isAuthorized(providedKey)) {
            chain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write("""
                {"title":"Unauthorized","status":401,"detail":"Missing or invalid %s header"}
                """.formatted(HEADER_NAME));
    }

    private boolean isAuthorized(String providedKey) {
        if (expectedApiKey == null || providedKey == null) {
            return false;
        }
        return MessageDigest.isEqual(expectedApiKey, providedKey.getBytes(StandardCharsets.UTF_8));
    }
}
