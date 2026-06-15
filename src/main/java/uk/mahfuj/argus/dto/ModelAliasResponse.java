package uk.mahfuj.argus.dto;

import java.time.Instant;


/**
 * Admin read response for a model alias. Denormalizes the target model id/name.
 */
public record ModelAliasResponse(
        Long id,
        String name,
        Long modelId,
        String modelName,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}
