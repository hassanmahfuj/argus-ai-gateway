package uk.mahfuj.argus.service.proxy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;


/**
 * Serializes an Argus-originated {@link ProxyError} into the SDK dialect of the
 * endpoint's {@link ApiShape} — both as an HTTP response body and as an SSE
 * {@code error} event (used when the response is already committed mid-stream).
 *
 * <p>HTTP bodies:
 * <ul>
 *   <li>OpenAI → {@code {"error":{"message","type","code"}}}</li>
 *   <li>Anthropic → {@code {"type":"error","error":{"type","message"}}}</li>
 * </ul>
 *
 * <p>SSE events:
 * <ul>
 *   <li>OpenAI → {@code data: {…}\n\n}</li>
 *   <li>Anthropic → {@code event: error\ndata: {…}\n\n}</li>
 * </ul>
 *
 * <p>The SDK {@code type} is derived from the HTTP status per dialect (see
 * {@link #typeFor}). Pure — no Spring web types — so it is unit-testable in isolation.
 */
@Component
public class ProxyErrorSerializer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public byte[] httpBody(final ApiShape shape, final ProxyError error) {
        return toJsonBytes(body(shape, error));
    }

    public byte[] sseEvent(final ApiShape shape, final ProxyError error) {
        final String json = toJsonString(body(shape, error));
        final String frame = shape == ApiShape.ANTHROPIC
                ? "event: error\ndata: " + json + "\n\n"
                : "data: " + json + "\n\n";
        return frame.getBytes(StandardCharsets.UTF_8);
    }

    private static Map<String, Object> body(final ApiShape shape, final ProxyError error) {
        final String type = typeFor(shape, error.status());
        if (shape == ApiShape.ANTHROPIC) {
            final Map<String, Object> inner = new LinkedHashMap<>();
            inner.put("type", type);
            inner.put("message", error.message());
            final Map<String, Object> root = new LinkedHashMap<>();
            root.put("type", "error");
            root.put("error", inner);
            return root;
        }
        final Map<String, Object> inner = new LinkedHashMap<>();
        inner.put("message", error.message());
        inner.put("type", type);
        inner.put("code", error.code());
        final Map<String, Object> root = new LinkedHashMap<>();
        root.put("error", inner);
        return root;
    }

    /** Maps an HTTP status to the SDK error {@code type} for the given dialect. */
    static String typeFor(final ApiShape shape, final int status) {
        if (shape == ApiShape.ANTHROPIC) {
            return switch (status) {
                case 400 -> "invalid_request_error";
                case 401 -> "authentication_error";
                case 403 -> "permission_error";
                case 404 -> "not_found_error";
                case 413 -> "request_too_large";
                case 429 -> "rate_limit_error";
                case 529 -> "overloaded_error";
                default -> status >= 500 ? "api_error" : "invalid_request_error";
            };
        }
        return switch (status) {
            case 401 -> "authentication_error";
            case 403 -> "permission_error";
            case 429 -> "rate_limit_error";
            default -> status >= 500 ? "api_error" : "invalid_request_error";
        };
    }

    private static byte[] toJsonBytes(final Object value) {
        try {
            return MAPPER.writeValueAsBytes(value);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String toJsonString(final Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
