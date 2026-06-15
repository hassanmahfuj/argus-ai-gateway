package uk.mahfuj.argus.service.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;

import uk.mahfuj.argus.exception.ResolutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Unit tests for {@link ProxyErrorHandler} — exception→{@link ProxyError} mapping and the
 * committed/SSE vs uncommitted/JSON branches, using a Mockito-mocked response whose
 * output stream captures the written bytes.
 */
class ProxyErrorHandlerTest {

    private final ProxyErrorSerializer serializer = new ProxyErrorSerializer();
    private final ProxyErrorHandler handler = new ProxyErrorHandler(serializer);

    @Test
    void resolution404OnOpenAi_setsStatusAndJsonBody() throws Exception {
        final Capture cap = new Capture();
        handler.handle(cap.response, new ResolutionException(404, "model_not_found", "Model not found: ghost"),
                ApiShape.OPENAI, "/v1/chat/completions");

        verify(cap.response).setStatus(404);
        verify(cap.response).setContentType("application/json");
        assertThat(cap.body()).contains("\"type\":\"invalid_request_error\"").contains("\"code\":\"model_not_found\"");
    }

    @Test
    void resolution404OnAnthropic_usesNotFoundError() throws Exception {
        final Capture cap = new Capture();
        handler.handle(cap.response, new ResolutionException(404, "model_not_found", "Model not found: ghost"),
                ApiShape.ANTHROPIC, "/v1/anthropic/v1/messages");

        verify(cap.response).setStatus(404);
        assertThat(cap.body()).contains("\"type\":\"not_found_error\"");
    }

    @Test
    void connectException_mapsTo502ApiError() throws Exception {
        final Capture cap = new Capture();
        handler.handle(cap.response, new ConnectException("refused"),
                ApiShape.OPENAI, "/v1/chat/completions");

        verify(cap.response).setStatus(502);
        assertThat(cap.body()).contains("\"type\":\"api_error\"").contains("\"code\":\"upstream_error\"");
    }

    @Test
    void committedEventStream_writesSseEventWithoutChangingStatus() throws Exception {
        final Capture cap = new Capture();
        when(cap.response.isCommitted()).thenReturn(true);
        when(cap.response.getContentType()).thenReturn("text/event-stream");

        handler.handle(cap.response, new ConnectException("dropped mid-stream"),
                ApiShape.ANTHROPIC, "/v1/anthropic/v1/messages");

        verify(cap.response, never()).setStatus(anyInt());
        assertThat(cap.body()).startsWith("event: error\ndata: ").contains("\"type\":\"api_error\"");
    }

    /** Wires a Mockito response to a {@link ServletOutputStream} that captures written bytes. */
    private static final class Capture {
        final HttpServletResponse response = mock(HttpServletResponse.class);
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        private final ServletOutputStream out = new ServletOutputStream() {
            @Override
            public void write(final int b) {
                baos.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(final WriteListener writeListener) {
                // no-op
            }
        };

        Capture() throws IOException {
            when(response.getOutputStream()).thenReturn(out);
        }

        String body() {
            return baos.toString(StandardCharsets.UTF_8);
        }
    }
}
