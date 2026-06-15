package uk.mahfuj.argus.service.token;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.mahfuj.argus.service.proxy.ApiShape;

/**
 * Strategy for extracting token usage from an upstream response. One implementation
 * per {@link ApiShape}; see {@link AnthropicTokenUsageExtractor} and
 * {@link OpenAiTokenUsageExtractor}. Selection is handled by
 * {@link TokenUsageExtractors}. Extraction is keyed on shape, not on the upstream
 * host — every OpenAI-shaped host (zai, xai, deepseek, …) reuses the OpenAI extractor.
 */
public interface TokenUsageExtractor {

    ObjectMapper MAPPER = new ObjectMapper();

    ApiShape shape();

    /** Extract usage from a buffered (non-streaming) upstream response body. */
    TokenUsage extract(byte[] responseBody);

    /** Create a fresh accumulator for an SSE (streaming) response. */
    SseTokenAccumulator newAccumulator();

    /** Extract the requested model from the request body — shared across shapes. */
    static String extractModel(final byte[] requestBody) {
        try {
            final JsonNode root = MAPPER.readTree(requestBody);
            return root.has("model") ? root.get("model").asText() : null;
        } catch (final Exception e) {
            return null;
        }
    }
}
