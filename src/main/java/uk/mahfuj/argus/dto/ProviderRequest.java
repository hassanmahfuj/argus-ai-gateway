package uk.mahfuj.argus.dto;

/**
 * Admin write payload for a provider. {@code apiKey} is plaintext and write-only:
 * required on create, optional on update (null ⇒ keep the existing key). Responses
 * never echo it back.
 */
public record ProviderRequest(
        String name,
        String apiKey,
        String openaiBaseUrl,
        String anthropicBaseUrl,
        Boolean enabled
) {}
