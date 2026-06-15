package uk.mahfuj.argus.service.token;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import uk.mahfuj.argus.service.proxy.Provider;


@Component
public class OpenAiTokenUsageExtractor implements TokenUsageExtractor {

    private static final Logger log = LoggerFactory.getLogger(OpenAiTokenUsageExtractor.class);

    @Override
    public Provider provider() {
        return Provider.OPENAI;
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
                if (usage.has("prompt_tokens")) {
                    inputTokens = usage.get("prompt_tokens").asInt();
                }
                if (usage.has("completion_tokens")) {
                    outputTokens = usage.get("completion_tokens").asInt();
                }
            }
            return new TokenUsage(model, inputTokens, outputTokens);
        } catch (final Exception e) {
            log.debug("Failed to extract OpenAI token data: {}", e.getMessage());
            return TokenUsage.empty();
        }
    }

    @Override
    public SseTokenAccumulator newAccumulator() {
        return new OpenAiSseTokenAccumulator();
    }

    /**
     * Accumulates token counts from OpenAI SSE events:
     * - First chunk may include usage info if stream_options.include_usage=true
     * - Final chunk with finish_reason contains usage
     */
    static final class OpenAiSseTokenAccumulator implements SseTokenAccumulator {

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

        @Override
        public TokenUsage toTokenUsage() {
            return new TokenUsage(model, inputTokens, outputTokens);
        }
    }
}
