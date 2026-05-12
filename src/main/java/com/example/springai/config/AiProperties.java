package com.example.springai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Läser AI-konfiguration från miljövariabler via application.properties.
 * Inga API-nycklar hårdkodas – allt injiceras via @ConfigurationProperties.
 *
 * Miljövariabler som krävs:
 *   AI_API_KEY  → apiKey
 *   AI_BASE_URL → baseUrl  (default: OpenRouter)
 *   AI_MODEL    → model    (default: gpt-4o-mini)
 */
@ConfigurationProperties(prefix = "ai")
public record AiProperties(
        String apiKey,
        String baseUrl,
        String model
) {}