package uk.mahfuj.argus.dto;

/**
 * Admin write payload for a model alias. {@code modelId} is required on create.
 * {@code name} must not contain a slash (it occupies the no-slash namespace).
 */
public record ModelAliasRequest(
        String name,
        Long modelId,
        Boolean enabled
) {}
