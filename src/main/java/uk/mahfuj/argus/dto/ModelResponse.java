package uk.mahfuj.argus.dto;

import java.time.Instant;


/**
 * Admin read response for a model. Denormalizes the provider name/id for convenience.
 */
public record ModelResponse(
        Long id,
        Long providerId,
        String providerName,
        String name,
        String upstreamModelName,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}
