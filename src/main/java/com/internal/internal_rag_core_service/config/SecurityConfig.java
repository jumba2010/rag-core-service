package com.internal.internal_rag_core_service.config;

import com.internal.internal_rag_core_service.security.ApiKeyAuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    @Bean
    public FilterRegistrationBean<ApiKeyAuthFilter> apiKeyAuthFilter(
            org.springframework.core.env.Environment env) {
        String apiKey = env.getRequiredProperty("app.security.api-key");
        FilterRegistrationBean<ApiKeyAuthFilter> registration =
                new FilterRegistrationBean<>(new ApiKeyAuthFilter(apiKey));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }
}
