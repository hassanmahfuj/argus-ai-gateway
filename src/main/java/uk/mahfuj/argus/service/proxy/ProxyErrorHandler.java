package uk.mahfuj.argus.service.proxy;

import java.io.IOException;
import java.net.ConnectException;
import java.net.http.HttpTimeoutException;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * Translates upstream/proxy failures into the gateway's structured JSON error
 * response ({@code {"error": "...", "message": "..."}}), used for the
 * {@code /v1/**} proxy endpoints.
 */
@Component
public class ProxyErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(ProxyErrorHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public void handle(final HttpServletResponse response, final Exception e, final String target) {
        try {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                writeError(response, 504, "upstream_timeout", "Request to upstream was interrupted");
            } else if (e instanceof ConnectException ce) {
                log.error("✗ upstream connection failed: {}", ce.getMessage());
                writeError(response, 502, "upstream_error", "Failed to connect to upstream: " + ce.getMessage());
            } else if (e instanceof HttpTimeoutException) {
                log.error("✗ upstream timeout: {}", target);
                writeError(response, 504, "upstream_timeout", "Upstream request timed out");
            } else {
                log.error("✗ proxy error: {}", e.getMessage(), e);
                writeError(response, 502, "upstream_error", e.getMessage());
            }
        } catch (final IOException ioex) {
            log.warn("Failed to write proxy error response: {}", ioex.getMessage());
        }
    }

    private void writeError(final HttpServletResponse response, final int status, final String error, final String message)
            throws IOException {
        if (!response.isCommitted()) {
            response.setStatus(status);
            response.setContentType("application/json");
            response.getOutputStream().write(
                    objectMapper.writeValueAsBytes(Map.of("error", error, "message", message))
            );
            response.getOutputStream().flush();
        }
    }
}
