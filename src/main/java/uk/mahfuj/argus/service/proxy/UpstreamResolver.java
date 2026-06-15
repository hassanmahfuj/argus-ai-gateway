package uk.mahfuj.argus.service.proxy;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import uk.mahfuj.argus.config.GatewayProperties;


/**
 * Maps an inbound request path to its upstream target. Requests under
 * {@code /v1/anthropic/**} route to the Anthropic upstream; everything else under
 * {@code /v1/**} routes to the OpenAI upstream. The {@code /v1} prefix is stripped
 * before forwarding, exactly as the original proxy did.
 */
@Component
public class UpstreamResolver {

    private static final String V1_PREFIX = "/v1";
    private static final String ANTHROPIC_PREFIX = "/v1/anthropic";

    private final GatewayProperties properties;

    public UpstreamResolver(final GatewayProperties properties) {
        this.properties = properties;
    }

    public ProxyTarget resolve(final HttpServletRequest request) {
        final String uri = request.getRequestURI();
        final String contextPath = request.getContextPath();
        final String full = uri.substring(contextPath.length());

        final Provider provider;
        final String upstreamBase;
        final String subPath;

        if (full.startsWith(ANTHROPIC_PREFIX)) {
            provider = Provider.ANTHROPIC;
            upstreamBase = properties.getUpstream().getAnthropic();
            subPath = full.substring(ANTHROPIC_PREFIX.length());
        } else if (full.startsWith(V1_PREFIX)) {
            provider = Provider.OPENAI;
            upstreamBase = properties.getUpstream().getOpenai();
            subPath = full.substring(V1_PREFIX.length());
        } else {
            // Should not happen given the /v1 controller mapping; route to OpenAI as a safe fallback.
            provider = Provider.OPENAI;
            upstreamBase = properties.getUpstream().getOpenai();
            subPath = full;
        }

        final String base = upstreamBase.replaceAll("/+$", "");
        final String tail = subPath.startsWith("/") ? subPath : "/" + subPath;
        final String query = request.getQueryString();
        final String targetUrl = base + tail + (query != null ? "?" + query : "");
        return new ProxyTarget(provider, targetUrl);
    }
}
