package uk.mahfuj.argus.service.proxy;

import uk.mahfuj.argus.service.token.TokenUsage;

/**
 * Outcome of forwarding a single request upstream, handed back to the
 * orchestrator so it can persist the request log.
 */
public record ProxyResult(
        int statusCode,
        long latencyMs,
        boolean streaming,
        String requestModel,
        TokenUsage tokens
) {}
