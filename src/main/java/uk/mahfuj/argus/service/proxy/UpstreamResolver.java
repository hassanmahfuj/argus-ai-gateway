package uk.mahfuj.argus.service.proxy;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;


/**
 * Builds the final upstream URL for a resolved target. The base URL and shape come
 * from the {@link ResolvedTarget} (resolved by {@link ModelResolver}); this class
 * only strips the inbound endpoint prefix ({@code /v1} for OpenAI shape,
 * {@code /v1/anthropic} for Anthropic shape) — exactly as the original proxy did —
 * and appends the subpath + query string to the base URL.
 */
@Component
public class UpstreamResolver {

    private static final String V1_PREFIX = "/v1";
    private static final String ANTHROPIC_PREFIX = "/v1/anthropic";

    public String buildTargetUrl(final HttpServletRequest request, final ResolvedTarget target) {
        final String uri = request.getRequestURI();
        final String contextPath = request.getContextPath();
        final String full = uri.substring(contextPath.length());

        final String prefix = target.shape() == ApiShape.ANTHROPIC ? ANTHROPIC_PREFIX : V1_PREFIX;
        final String subPath = full.startsWith(prefix) ? full.substring(prefix.length()) : full;

        final String base = target.baseUrl();
        final String tail = subPath.startsWith("/") ? subPath : "/" + subPath;
        final String query = request.getQueryString();
        return base + tail + (query != null ? "?" + query : "");
    }
}
