package uk.mahfuj.argus.service.token;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.mahfuj.argus.service.proxy.Provider;

/**
 * Strategy for extracting token usage from an upstream AI provider's responses.
 * One implementation per {@link Provider}; see {@link AnthropicTokenUsageExtractor}
 * and {@link OpenAiTokenUsageExtractor}. Selection is handled by
 * {@link TokenUsageExtractors}.
 */
public interface TokenUsageExtractor {

    ObjectMapper MAPPER = new ObjectMapper();

    Provider provider();

    /** Extract usage from a buffered (non-streaming) upstream response body. */
    TokenUsage extract(byte[] responseBody);

    /** Create a fresh accumulator for an SSE (streaming) response. */
    SseTokenAccumulator newAccumulator();

    /** Extract the requested model from the request body — shared across providers. */
    static String extractModel(final byte[] requestBody) {
        try {
            final JsonNode root = MAPPER.readTree(requestBody);
            return root.has("model") ? root.get("model").asText() : null;
        } catch (final Exception e) {
            return null;
        }
    }
}
