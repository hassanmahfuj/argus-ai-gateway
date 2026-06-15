package uk.mahfuj.argus.service;

import java.io.IOException;
import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import uk.mahfuj.argus.entity.ApiRequestLogEntity;
import uk.mahfuj.argus.service.proxy.ProxyErrorHandler;
import uk.mahfuj.argus.service.proxy.ProxyExecutor;
import uk.mahfuj.argus.service.proxy.ProxyResult;
import uk.mahfuj.argus.service.proxy.ProxyTarget;
import uk.mahfuj.argus.service.proxy.Provider;
import uk.mahfuj.argus.service.proxy.UpstreamResolver;
import uk.mahfuj.argus.service.token.TokenUsage;
import uk.mahfuj.argus.service.token.TokenUsageExtractor;
import uk.mahfuj.argus.service.token.TokenUsageExtractors;


/**
 * Orchestrates proxying of a single inbound request: resolve the upstream target,
 * forward it (delegated to {@link ProxyExecutor}), persist the request log, and
 * route any failure to {@link ProxyErrorHandler}. Routing, forwarding, token
 * extraction and error handling each live in their own collaborators.
 */
@Service
public class GatewayProxyService {

    private static final Logger log = LoggerFactory.getLogger(GatewayProxyService.class);

    private final UpstreamResolver upstreamResolver;
    private final ProxyExecutor proxyExecutor;
    private final ProxyErrorHandler errorHandler;
    private final TokenUsageExtractors extractors;
    private final RequestLogService requestLogService;

    public GatewayProxyService(final UpstreamResolver upstreamResolver,
                               final ProxyExecutor proxyExecutor,
                               final ProxyErrorHandler errorHandler,
                               final TokenUsageExtractors extractors,
                               final RequestLogService requestLogService) {
        this.upstreamResolver = upstreamResolver;
        this.proxyExecutor = proxyExecutor;
        this.errorHandler = errorHandler;
        this.extractors = extractors;
        this.requestLogService = requestLogService;
    }

    public void proxy(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final ProxyTarget target = upstreamResolver.resolve(request);
        final TokenUsageExtractor extractor = extractors.forProvider(target.provider());
        try {
            final ProxyResult result = proxyExecutor.execute(request, response, target, extractor);
            persistLog(request, target.provider(), result);
        } catch (final Exception e) {
            errorHandler.handle(response, e, target.targetUrl());
        }
    }

    private void persistLog(final HttpServletRequest request, final Provider provider, final ProxyResult result) {
        try {
            final TokenUsage tokens = result.tokens();
            final String model = (tokens != null && tokens.model() != null) ? tokens.model() : result.requestModel();
            final ApiRequestLogEntity logEntry = new ApiRequestLogEntity();
            logEntry.setTimestamp(Instant.now());
            logEntry.setProvider(provider.dbValue());
            logEntry.setModel(model);
            logEntry.setInputTokens(tokens != null ? tokens.inputTokens() : 0);
            logEntry.setOutputTokens(tokens != null ? tokens.outputTokens() : 0);
            logEntry.setRequestMethod(request.getMethod());
            logEntry.setRequestPath(request.getRequestURI());
            logEntry.setResponseStatus(result.statusCode());
            logEntry.setLatencyMs(result.latencyMs());
            logEntry.setIsStreaming(result.streaming());
            requestLogService.save(logEntry);
        } catch (final Exception e) {
            log.warn("Failed to log token usage: {}", e.getMessage());
        }
    }
}
