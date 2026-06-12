package uk.mahfuj.aigateway.service;

import uk.mahfuj.aigateway.config.GatewayProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.net.http.HttpClient.Version;


@Service
public class GatewayProxyService {

    private static final Logger log = LoggerFactory.getLogger(GatewayProxyService.class);
    private static final Set<String> HOP_BY_HOP = Set.of(
            "host", "connection", "transfer-encoding", "content-length"
    );
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final GatewayProperties properties;
    private final HttpClient httpClient;

    public GatewayProxyService(final GatewayProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .version(Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(properties.getTimeout()))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public void forward(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final String targetBase,
            final String subPath
    ) throws IOException {
        final String query = request.getQueryString();
        final String base = targetBase.replaceAll("/+$", "");
        final String tail = subPath.startsWith("/") ? subPath : "/" + subPath;
        final String target = base + tail + (query != null ? "?" + query : "");

        final byte[] body = request.getInputStream().readAllBytes();
        final String bodyPreview = new String(body).substring(0, Math.min(body.length, 200));
        logRequest(request, target, bodyPreview);

        final Map<String, String> headers = new LinkedHashMap<>();
        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String name = headerNames.nextElement();
            if (!shouldForwardHeader(name)) {
                continue;
            }
            headers.put(name, request.getHeader(name));
        }

        if (properties.getApiKey() != null && !properties.getApiKey().isBlank()) {
            headers.put("Authorization", "Bearer " + properties.getApiKey());
        }

        try {
            final HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(target))
                    .method(request.getMethod(), HttpRequest.BodyPublishers.ofByteArray(body))
                    .timeout(Duration.ofSeconds(properties.getTimeout()));

            headers.forEach(reqBuilder::header);

            final long start = System.currentTimeMillis();
            final HttpResponse<java.io.InputStream> proxyResponse = httpClient.send(
                    reqBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream()
            );
            final long ms = System.currentTimeMillis() - start;

            response.setStatus(proxyResponse.statusCode());

            proxyResponse.headers().map().forEach((name, values) -> {
                if (shouldForwardHeader(name)) {
                    values.forEach(v -> response.addHeader(name, v));
                }
            });

            final boolean isSse = (proxyResponse.headers()
                    .firstValue("content-type")
                    .orElse("")
                    .contains("text/event-stream"));

            if (isSse) {
                log.info("← {} ({}ms) {} [streaming]", proxyResponse.statusCode(), ms, request.getRequestURI());
                try (final java.io.InputStream in = proxyResponse.body()) {
                    in.transferTo(response.getOutputStream());
                    response.getOutputStream().flush();
                }
            } else {
                try (final java.io.InputStream in = proxyResponse.body()) {
                    final byte[] resBody = in.readAllBytes();
                    log.info("← {} ({}ms) {}", proxyResponse.statusCode(), ms, request.getRequestURI());
                    log.debug("  response: {}", new String(resBody).substring(0, Math.min(resBody.length, 300)));
                    response.getOutputStream().write(resBody);
                    response.getOutputStream().flush();
                }
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            writeError(response, 504, "upstream_timeout", "Request to upstream was interrupted");
        } catch (final java.net.ConnectException e) {
            log.error("✗ upstream connection failed: {}", e.getMessage());
            writeError(response, 502, "upstream_error", "Failed to connect to upstream: " + e.getMessage());
        } catch (final java.net.http.HttpTimeoutException e) {
            log.error("✗ upstream timeout: {}", target);
            writeError(response, 504, "upstream_timeout", "Upstream request timed out");
        } catch (final Exception e) {
            log.error("✗ proxy error: {}", e.getMessage(), e);
            writeError(response, 502, "upstream_error", e.getMessage());
        }
    }

    public String extractSubPath(final HttpServletRequest request, final String prefix) {
        final String uri = request.getRequestURI();
        final String contextPath = request.getContextPath();
        final String full = uri.substring(contextPath.length());
        if (full.startsWith(prefix)) {
            return full.substring(prefix.length());
        }
        return full;
    }

    private static boolean shouldForwardHeader(final String name) {
        final String lower = name.toLowerCase();
        return !lower.startsWith(":") && !HOP_BY_HOP.contains(lower);
    }

    private void writeError(
            final HttpServletResponse response,
            final int status,
            final String error,
            final String message
    ) throws IOException {
        if (!response.isCommitted()) {
            response.setStatus(status);
            response.setContentType("application/json");
            response.getOutputStream().write(
                    objectMapper.writeValueAsBytes(Map.of("error", error, "message", message))
            );
            response.getOutputStream().flush();
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
        } catch (Exception e) {
            log.info("  body: {}", bodyPreview);
        }
    }
}
