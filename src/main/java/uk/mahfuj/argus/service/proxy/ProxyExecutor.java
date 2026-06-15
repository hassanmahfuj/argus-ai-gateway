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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import uk.mahfuj.argus.config.GatewayProperties;
import uk.mahfuj.argus.service.token.SseTokenAccumulator;
import uk.mahfuj.argus.service.token.TokenUsage;
import uk.mahfuj.argus.service.token.TokenUsageExtractor;


/**
 * Performs the actual upstream HTTP exchange: header mapping (with hop-by-hop
 * filtering and bearer-token injection), the send, response status/header copy,
 * and streaming (SSE) or buffered body forwarding with token extraction.
 * Returns a {@link ProxyResult}; the orchestrator owns persistence and errors.
 */
@Component
public class ProxyExecutor {

    private static final Logger log = LoggerFactory.getLogger(ProxyExecutor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final GatewayProperties properties;
    private final HttpClient httpClient;

    public ProxyExecutor(final GatewayProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(properties.getTimeout()))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public ProxyResult execute(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final ProxyTarget target,
            final TokenUsageExtractor extractor
    ) throws IOException, InterruptedException {

        final String targetUrl = target.targetUrl();
        final byte[] body = request.getInputStream().readAllBytes();
        final String bodyPreview = new String(body).substring(0, Math.min(body.length, 200));
        final String requestModel = TokenUsageExtractor.extractModel(body);
        logRequest(request, targetUrl, bodyPreview);

        final Map<String, String> headers = new LinkedHashMap<>();
        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String name = headerNames.nextElement();
            if (ProxyHeaders.shouldForward(name)) {
                headers.put(name, request.getHeader(name));
            }
        }

        if (properties.getApiKey() != null && !properties.getApiKey().isBlank()) {
            headers.put("Authorization", "Bearer " + properties.getApiKey());
        }

        final HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .method(request.getMethod(), HttpRequest.BodyPublishers.ofByteArray(body))
                .timeout(Duration.ofSeconds(properties.getTimeout()));
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
            return new ProxyResult(proxyResponse.statusCode(), ms, true, requestModel, tokens);
        }

        try (final InputStream in = proxyResponse.body()) {
            final byte[] resBody = in.readAllBytes();
            log.info("← {} ({}ms) {}", proxyResponse.statusCode(), ms, request.getRequestURI());
            log.debug("  response: {}", new String(resBody).substring(0, Math.min(resBody.length, 300)));
            response.getOutputStream().write(resBody);
            response.getOutputStream().flush();

            final TokenUsage tokens = extractor.extract(resBody);
            return new ProxyResult(proxyResponse.statusCode(), ms, false, requestModel, tokens);
        }
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
