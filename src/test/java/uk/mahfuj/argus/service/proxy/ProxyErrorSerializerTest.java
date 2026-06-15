package uk.mahfuj.argus.service.proxy;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Pure unit tests for {@link ProxyErrorSerializer} — verifies the status→SDK-{@code type}
 * mapping and the OpenAI vs Anthropic body/SSE framing, with no Spring context.
 */
class ProxyErrorSerializerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final ProxyErrorSerializer serializer = new ProxyErrorSerializer();

    private static JsonNode parse(final byte[] bytes) throws Exception {
        return MAPPER.readTree(bytes);
    }

    @Test
    void openAi404IsInvalidRequestErrorWithCode() throws Exception {
        final JsonNode root = parse(serializer.httpBody(ApiShape.OPENAI,
                new ProxyError(404, "model_not_found", "Model not found: ghost")));
        assertThat(root.path("error").path("type").asText()).isEqualTo("invalid_request_error");
        assertThat(root.path("error").path("code").asText()).isEqualTo("model_not_found");
        assertThat(root.path("error").path("message").asText()).isEqualTo("Model not found: ghost");
    }

    @Test
    void openAi5xxIsApiError() throws Exception {
        final JsonNode root = parse(serializer.httpBody(ApiShape.OPENAI,
                new ProxyError(502, "upstream_error", "boom")));
        assertThat(root.path("error").path("type").asText()).isEqualTo("api_error");
        assertThat(root.path("error").path("code").asText()).isEqualTo("upstream_error");
    }

    @Test
    void anthropic404IsNotFoundErrorAndHasNoCodeField() throws Exception {
        final JsonNode root = parse(serializer.httpBody(ApiShape.ANTHROPIC,
                new ProxyError(404, "model_not_found", "Model not found: ghost")));
        assertThat(root.path("type").asText()).isEqualTo("error");
        assertThat(root.path("error").path("type").asText()).isEqualTo("not_found_error");
        assertThat(root.path("error").has("code")).isFalse();
        assertThat(root.path("error").path("message").asText()).isEqualTo("Model not found: ghost");
    }

    @Test
    void anthropic529IsOverloadedError() throws Exception {
        final JsonNode root = parse(serializer.httpBody(ApiShape.ANTHROPIC,
                new ProxyError(529, "upstream_error", "overloaded")));
        assertThat(root.path("error").path("type").asText()).isEqualTo("overloaded_error");
    }

    @Test
    void rateLimitIsRateLimitErrorOnBothShapes() throws Exception {
        assertThat(parse(serializer.httpBody(ApiShape.OPENAI, new ProxyError(429, "rate_limited", "slow")))
                .path("error").path("type").asText()).isEqualTo("rate_limit_error");
        assertThat(parse(serializer.httpBody(ApiShape.ANTHROPIC, new ProxyError(429, "rate_limited", "slow")))
                .path("error").path("type").asText()).isEqualTo("rate_limit_error");
    }

    @Test
    void openAiSseEventIsDataFramed() throws Exception {
        final String s = new String(serializer.sseEvent(ApiShape.OPENAI,
                new ProxyError(502, "upstream_error", "boom")), StandardCharsets.UTF_8);
        assertThat(s).startsWith("data: ").endsWith("\n\n");
        final JsonNode root = MAPPER.readTree(s.substring("data: ".length()).trim());
        assertThat(root.path("error").path("type").asText()).isEqualTo("api_error");
    }

    @Test
    void anthropicSseEventIsEventErrorFramed() throws Exception {
        final String s = new String(serializer.sseEvent(ApiShape.ANTHROPIC,
                new ProxyError(502, "upstream_error", "boom")), StandardCharsets.UTF_8);
        assertThat(s).startsWith("event: error\ndata: ").endsWith("\n\n");
        final JsonNode root = MAPPER.readTree(s.substring(s.indexOf("data: ") + "data: ".length()).trim());
        assertThat(root.path("type").asText()).isEqualTo("error");
        assertThat(root.path("error").path("type").asText()).isEqualTo("api_error");
    }
}
