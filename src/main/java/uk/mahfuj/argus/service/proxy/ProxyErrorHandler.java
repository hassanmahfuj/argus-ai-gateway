package uk.mahfuj.argus.service.proxy;

import java.io.IOException;
import java.net.ConnectException;
import java.net.http.HttpTimeoutException;

import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import uk.mahfuj.argus.exception.ResolutionException;


/**
 * Translates a proxy-path ({@code /v1/**}) failure into an SDK-correct error for the
 * endpoint's {@link ApiShape}. Argus-originated failures (resolution + transport) are
 * rendered by {@link ProxyErrorSerializer} in the dialect the SDK parses — OpenAI or
 * Anthropic, as an HTTP body normally, or as an SSE {@code error} event when the
 * response is already committed mid-stream.
 *
 * <p>Upstream HTTP errors are NOT handled here: {@code ProxyExecutor} forwards those
 * verbatim, because the upstream already speaks the SDK dialect.
 */
@Component
public class ProxyErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(ProxyErrorHandler.class);

    private final ProxyErrorSerializer serializer;

    public ProxyErrorHandler(final ProxyErrorSerializer serializer) {
        this.serializer = serializer;
    }

    public void handle(final HttpServletResponse response, final Exception e, final ApiShape shape, final String target) {
        final ProxyError error = toProxyError(e, target);
        try {
            if (response.isCommitted() && isEventStream(response)) {
                // Streaming response already started (200 + event-stream sent); can't change
                // status — emit a terminal SSE error event so the client isn't left truncated.
                response.getOutputStream().write(serializer.sseEvent(shape, error));
                response.getOutputStream().flush();
                return;
            }
            if (response.isCommitted()) {
                // Non-SSE committed response (shouldn't happen for our errors); nothing safe to do.
                return;
            }
            response.setStatus(error.status());
            response.setContentType("application/json");
            response.getOutputStream().write(serializer.httpBody(shape, error));
            response.getOutputStream().flush();
        } catch (final IOException ioex) {
            log.warn("Failed to write proxy error response: {}", ioex.getMessage());
        }
    }

    private static boolean isEventStream(final HttpServletResponse response) {
        final String ct = response.getContentType();
        return ct != null && ct.contains("text/event-stream");
    }

    private static ProxyError toProxyError(final Exception e, final String target) {
        if (e instanceof final ResolutionException re) {
            return new ProxyError(re.status(), re.code(), re.getMessage());
        }
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
            return new ProxyError(504, "upstream_timeout", "Request to upstream was interrupted");
        }
        if (e instanceof final ConnectException ce) {
            log.error("✗ upstream connection failed: {}", ce.getMessage());
            return new ProxyError(502, "upstream_error", "Failed to connect to upstream: " + ce.getMessage());
        }
        if (e instanceof HttpTimeoutException) {
            log.error("✗ upstream timeout: {}", target);
            return new ProxyError(504, "upstream_timeout", "Upstream request timed out");
        }
        log.error("✗ proxy error: {}", e.getMessage(), e);
        return new ProxyError(502, "upstream_error", e.getMessage());
    }
}
