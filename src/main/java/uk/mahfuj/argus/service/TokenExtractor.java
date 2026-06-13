package uk.mahfuj.argus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


final class TokenExtractor {

    private static final Logger log = LoggerFactory.getLogger(TokenExtractor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private TokenExtractor() {}

    record TokenData(String model, int inputTokens, int outputTokens) {
        static TokenData empty() {
            return new TokenData(null, 0, 0);
        }
    }

    static TokenData extractFromAnthropicResponse(final byte[] responseBody) {
        try {
            final JsonNode root = objectMapper.readTree(responseBody);
            final String model = root.has("model") ? root.get("model").asText() : null;
            int inputTokens = 0;
            int outputTokens = 0;
            if (root.has("usage")) {
                final JsonNode usage = root.get("usage");
                if (usage.has("input_tokens")) {
                    inputTokens = usage.get("input_tokens").asInt();
                }
                if (usage.has("output_tokens")) {
                    outputTokens = usage.get("output_tokens").asInt();
                }
            }
            return new TokenData(model, inputTokens, outputTokens);
        } catch (final Exception e) {
            log.debug("Failed to extract Anthropic token data: {}", e.getMessage());
            return TokenData.empty();
        }
    }

    static TokenData extractFromOpenAIResponse(final byte[] responseBody) {
        try {
            final JsonNode root = objectMapper.readTree(responseBody);
            final String model = root.has("model") ? root.get("model").asText() : null;
            int inputTokens = 0;
            int outputTokens = 0;
            if (root.has("usage")) {
                final JsonNode usage = root.get("usage");
                if (usage.has("prompt_tokens")) {
                    inputTokens = usage.get("prompt_tokens").asInt();
                }
                if (usage.has("completion_tokens")) {
                    outputTokens = usage.get("completion_tokens").asInt();
                }
            }
            return new TokenData(model, inputTokens, outputTokens);
        } catch (final Exception e) {
            log.debug("Failed to extract OpenAI token data: {}", e.getMessage());
            return TokenData.empty();
        }
    }

    static String extractModelFromRequest(final byte[] requestBody) {
        try {
            final JsonNode root = objectMapper.readTree(requestBody);
            return root.has("model") ? root.get("model").asText() : null;
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Accumulates token counts from SSE events for Anthropic streaming responses.
     * Anthropic SSE events:
     * - message_start: contains message.usage.input_tokens (and output_tokens=0)
     * - message_delta: contains usage.output_tokens (cumulative)
     * - Also extracts model from message_start
     */
    static class SseTokenAccumulator {
        private String model;
        private int inputTokens;
        private int outputTokens;

        void processLine(final String line) {
            if (line == null || !line.startsWith("data:")) {
                return;
            }
            final String json = line.substring(5).trim();
            if (json.isEmpty() || "[DONE]".equals(json)) {
                return;
            }
            try {
                final JsonNode root = objectMapper.readTree(json);
                final String type = root.has("type") ? root.get("type").asText() : "";

                if ("message_start".equals(type)) {
                    // Anthropic: { type: "message_start", message: { model, usage: { input_tokens, output_tokens } } }
                    if (root.has("message")) {
                        final JsonNode message = root.get("message");
                        if (message.has("model")) {
                            model = message.get("model").asText();
                        }
                        if (message.has("usage")) {
                            final JsonNode usage = message.get("usage");
                            inputTokens = usage.has("input_tokens") ? usage.get("input_tokens").asInt() : inputTokens;
                            outputTokens = usage.has("output_tokens") ? usage.get("output_tokens").asInt() : outputTokens;
                        }
                    }
                } else if ("message_delta".equals(type)) {
                    // Anthropic: { type: "message_delta", usage: { output_tokens } }
                    if (root.has("usage")) {
                        final JsonNode usage = root.get("usage");
                        if (usage.has("output_tokens")) {
                            outputTokens = usage.get("output_tokens").asInt();
                        }
                    }
                }
            } catch (final Exception e) {
                // Not all data lines are valid JSON; skip silently
            }
        }

        /**
         * Process an OpenAI SSE line for token extraction.
         * OpenAI SSE events:
         * - First chunk may include usage info if stream_options.include_usage=true
         * - Final chunk with finish_reason contains usage
         */
        void processOpenAiLine(final String line) {
            if (line == null || !line.startsWith("data:")) {
                return;
            }
            final String json = line.substring(5).trim();
            if (json.isEmpty() || "[DONE]".equals(json)) {
                return;
            }
            try {
                final JsonNode root = objectMapper.readTree(json);
                if (root.has("model") && model == null) {
                    model = root.get("model").asText();
                }
                if (root.has("usage") && !root.get("usage").isNull()) {
                    final JsonNode usage = root.get("usage");
                    if (usage.has("prompt_tokens")) {
                        inputTokens = usage.get("prompt_tokens").asInt();
                    }
                    if (usage.has("completion_tokens")) {
                        outputTokens = usage.get("completion_tokens").asInt();
                    }
                }
            } catch (final Exception e) {
                // skip
            }
        }

        TokenData toTokenData() {
            return new TokenData(model, inputTokens, outputTokens);
        }
    }
}
