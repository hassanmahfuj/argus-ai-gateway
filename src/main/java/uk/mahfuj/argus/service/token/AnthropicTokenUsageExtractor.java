package uk.mahfuj.argus.service.token;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import uk.mahfuj.argus.service.proxy.ApiShape;


@Component
public class AnthropicTokenUsageExtractor implements TokenUsageExtractor {

    private static final Logger log = LoggerFactory.getLogger(AnthropicTokenUsageExtractor.class);

    @Override
    public ApiShape shape() {
        return ApiShape.ANTHROPIC;
    }

    @Override
    public TokenUsage extract(final byte[] responseBody) {
        try {
            final JsonNode root = MAPPER.readTree(responseBody);
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
            return new TokenUsage(model, inputTokens, outputTokens);
        } catch (final Exception e) {
            log.debug("Failed to extract Anthropic token data: {}", e.getMessage());
            return TokenUsage.empty();
        }
    }

    @Override
    public SseTokenAccumulator newAccumulator() {
        return new AnthropicSseTokenAccumulator();
    }

    /**
     * Accumulates token counts from Anthropic SSE events:
     * - message_start: contains message.usage.input_tokens (and output_tokens=0)
     * - message_delta: contains usage.output_tokens (cumulative)
     * Also extracts model from message_start.
     */
    static final class AnthropicSseTokenAccumulator implements SseTokenAccumulator {

        private String model;
        private int inputTokens;
        private int outputTokens;

        @Override
        public void processLine(final String line) {
            if (line == null || !line.startsWith("data:")) {
                return;
            }
            final String json = line.substring(5).trim();
            if (json.isEmpty() || "[DONE]".equals(json)) {
                return;
            }
            try {
                final JsonNode root = MAPPER.readTree(json);
                final String type = root.has("type") ? root.get("type").asText() : "";

                if ("message_start".equals(type)) {
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

        @Override
        public TokenUsage toTokenUsage() {
            return new TokenUsage(model, inputTokens, outputTokens);
        }
    }
}
