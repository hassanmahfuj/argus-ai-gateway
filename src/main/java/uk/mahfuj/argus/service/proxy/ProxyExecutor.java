package uk.mahfuj.argus.service.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.mahfuj.argus.service.token.SseTokenAccumulator;
import uk.mahfuj.argus.service.token.TokenUsage;
import uk.mahfuj.argus.service.token.TokenUsageExtractor;


/**
 * Performs the actual upstream HTTP exchange for a resolved target: rewrites the
 * request body's {@code model} field to the upstream name, copies headers (with
 * hop-by-hop filtering) and injects the resolved provider's bearer token, sends,
 * then streams (SSE) or buffers the response while extracting token usage. Returns
 * a {@link ProxyResult}; the orchestrator owns persistence and errors.
 */
@Component
public class ProxyExecutor {

    private static final Logger log = LoggerFactory.getLogger(ProxyExecutor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final UpstreamResolver upstreamResolver;
    private final int timeoutSeconds;
    private final HttpClient httpClient;

    public ProxyExecutor(final UpstreamResolver upstreamResolver,
                         @Value("${argus.proxy.timeout:120}") final int timeoutSeconds) {
        this.upstreamResolver = upstreamResolver;
        this.timeoutSeconds = timeoutSeconds;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public ProxyResult execute(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final ResolvedTarget target,
            final TokenUsageExtractor extractor,
            final byte[] rawBody
    ) throws IOException, InterruptedException {

        final String targetUrl = upstreamResolver.buildTargetUrl(request, target);
        final byte[] body = rewriteModel(rawBody, target.upstreamModel());
        final String bodyPreview = new String(rawBody).substring(0, Math.min(rawBody.length, 200));
        logRequest(request, targetUrl, bodyPreview);

        final Map<String, String> headers = new LinkedHashMap<>();
        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String name = headerNames.nextElement();
            if (ProxyHeaders.shouldForward(name)) {
                headers.put(name, request.getHeader(name));
            }
        }
        headers.put("Authorization", "Bearer " + target.apiKey());

        final HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .method(request.getMethod(), HttpRequest.BodyPublishers.ofByteArray(body))
                .timeout(Duration.ofSeconds(timeoutSeconds));
        headers.forEach(reqBuilder::header);

        final long start = System.currentTimeMillis();
        final HttpResponse<InputStream> proxyResponse = httpClient.send(
                reqBuilder.build(),
                HttpResponse.BodyHandlers.ofInputStream()
        );
        final long ms = System.currentTimeMillis() - start;

        response.setStatus(proxyResponse.statusCode());

        proxyResponse.headers().map().forEach((name, values) -> {
            if (ProxyHeaders.shouldForward(name)) {
                values.forEach(v -> response.addHeader(name, v));
            }
        });

        final boolean isSse = proxyResponse.headers()
                .firstValue("content-type")
                .orElse("")
                .contains("text/event-stream");

        if (isSse) {
            log.info("← {} ({}ms) {} [streaming]", proxyResponse.statusCode(), ms, request.getRequestURI());
            final SseTokenAccumulator accumulator = extractor.newAccumulator();
            try (final InputStream in = proxyResponse.body()) {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                final var out = response.getOutputStream();
                String line;
                while ((line = reader.readLine()) != null) {
                    accumulator.processLine(line);
                    out.write((line + "\n").getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }
            }
            final TokenUsage tokens = accumulator.toTokenUsage();
            return new ProxyResult(proxyResponse.statusCode(), ms, true, target.upstreamModel(), tokens);
        }

        try (final InputStream in = proxyResponse.body()) {
            final byte[] resBody = in.readAllBytes();
            log.info("← {} ({}ms) {}", proxyResponse.statusCode(), ms, request.getRequestURI());
            log.debug("  response: {}", new String(resBody).substring(0, Math.min(resBody.length, 300)));
            response.getOutputStream().write(resBody);
            response.getOutputStream().flush();

            final TokenUsage tokens = extractor.extract(resBody);
            return new ProxyResult(proxyResponse.statusCode(), ms, false, target.upstreamModel(), tokens);
        }
    }

    /**
     * Rewrites the top-level {@code model} field to the upstream name. Both the
     * OpenAI and Anthropic shapes carry {@code model} at the top level, so one
     * transform covers both. If the body isn't valid JSON or lacks the field, it is
     * forwarded unchanged — Argus never invents a model.
     */
    static byte[] rewriteModel(final byte[] body, final String upstreamModel) {
        if (upstreamModel == null || body == null || body.length == 0) {
            return body;
        }
        try {
            final JsonNode root = objectMapper.readTree(body);
            if (root.isObject() && root.has("model")) {
                ((ObjectNode) root).put("model", upstreamModel);
                return objectMapper.writeValueAsBytes(root);
            }
        } catch (final Exception e) {
            log.debug("Could not rewrite model field in request body: {}", e.getMessage());
        }
        return body;
    }

    private void logRequest(final HttpServletRequest request, final String target, final String bodyPreview) {
        log.info("→ {} {}  →  {}", request.getMethod(), request.getRequestURI(), target);
        try {
            final var node = objectMapper.readTree(bodyPreview);
            final var msg = new LinkedHashMap<String, Object>();
            if (node.has("model")) msg.put("model", node.get("model").asText());
            if (node.has("stream")) msg.put("stream", node.get("stream").asBoolean());
            if (node.has("messages")) msg.put("messages", node.get("messages").size());
            if (node.has("max_tokens")) msg.put("max_tokens", node.get("max_tokens").asInt());
            log.info("  body: {}", objectMapper.writeValueAsString(msg));
        } catch (final Exception e) {
            log.info("  body: {}", bodyPreview);
        }
    }
}
