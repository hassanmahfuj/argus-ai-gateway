package uk.mahfuj.argus.service.token;

/**
 * Extracted token-usage figures for a single proxied request.
 * Internal value object (not a controller-facing DTO).
 */
public record TokenUsage(
        String model,
        int inputTokens,
        int outputTokens
) {

    public static TokenUsage empty() {
        return new TokenUsage(null, 0, 0);
    }
}
