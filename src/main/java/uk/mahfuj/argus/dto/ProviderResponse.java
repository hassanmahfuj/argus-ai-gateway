package uk.mahfuj.argus.dto;

import java.time.Instant;


/**
 * Admin read response for a provider. Never exposes the API key — only whether one
 * is set.
 */
public record ProviderResponse(
        Long id,
        String name,
        String openaiBaseUrl,
        String anthropicBaseUrl,
        boolean enabled,
        boolean apiKeySet,
        Instant createdAt,
        Instant updatedAt
) {}
