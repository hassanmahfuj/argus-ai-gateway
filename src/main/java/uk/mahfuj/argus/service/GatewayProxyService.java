package uk.mahfuj.argus.service;

import java.io.IOException;
import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import uk.mahfuj.argus.entity.ApiRequestLogEntity;
import uk.mahfuj.argus.service.proxy.ApiShape;
import uk.mahfuj.argus.service.proxy.ModelResolver;
import uk.mahfuj.argus.service.proxy.ProxyErrorHandler;
import uk.mahfuj.argus.service.proxy.ProxyExecutor;
import uk.mahfuj.argus.service.proxy.ProxyResult;
import uk.mahfuj.argus.service.proxy.ResolvedTarget;
import uk.mahfuj.argus.service.token.TokenUsage;
import uk.mahfuj.argus.service.token.TokenUsageExtractor;
import uk.mahfuj.argus.service.token.TokenUsageExtractors;


/**
 * Orchestrates a single proxied request: derive the endpoint shape from the URL,
 * read the request body once (to extract the client {@code model} and forward it),
 * resolve the target against the catalog, delegate forwarding to
 * {@link ProxyExecutor}, persist the request log, and route any failure to
 * {@link ProxyErrorHandler} in the endpoint's native shape.
 */
@Service
public class GatewayProxyService {

    private static final Logger log = LoggerFactory.getLogger(GatewayProxyService.class);
    private static final String ANTHROPIC_PREFIX = "/v1/anthropic";

    private final ModelResolver modelResolver;
    private final ProxyExecutor proxyExecutor;
    private final ProxyErrorHandler errorHandler;
    private final TokenUsageExtractors extractors;
    private final RequestLogService requestLogService;

    public GatewayProxyService(final ModelResolver modelResolver,
                               final ProxyExecutor proxyExecutor,
                               final ProxyErrorHandler errorHandler,
                               final TokenUsageExtractors extractors,
                               final RequestLogService requestLogService) {
        this.modelResolver = modelResolver;
        this.proxyExecutor = proxyExecutor;
        this.errorHandler = errorHandler;
        this.extractors = extractors;
        this.requestLogService = requestLogService;
    }

    public void proxy(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final ApiShape shape = shapeOf(request);
        final byte[] body = request.getInputStream().readAllBytes();
        final String clientModel = TokenUsageExtractor.extractModel(body);

        try {
            final ResolvedTarget target = modelResolver.resolve(shape, clientModel);
            final TokenUsageExtractor extractor = extractors.forShape(target.shape());
            final ProxyResult result = proxyExecutor.execute(request, response, target, extractor, body);
            persistLog(request, target, result);
        } catch (final Exception e) {
            errorHandler.handle(response, e, shape, request.getRequestURI());
        }
    }

    private static ApiShape shapeOf(final HttpServletRequest request) {
        final String full = request.getRequestURI().substring(request.getContextPath().length());
        return full.startsWith(ANTHROPIC_PREFIX) ? ApiShape.ANTHROPIC : ApiShape.OPENAI;
    }

    private void persistLog(final HttpServletRequest request, final ResolvedTarget target, final ProxyResult result) {
        try {
            final TokenUsage tokens = result.tokens();
            final String model = (tokens != null && tokens.model() != null) ? tokens.model() : result.requestModel();
            final ApiRequestLogEntity logEntry = new ApiRequestLogEntity();
            logEntry.setTimestamp(Instant.now());
            logEntry.setProvider(target.providerName());
            logEntry.setModel(model);
            logEntry.setRequestedModel(target.requestedModel());
            logEntry.setInputTokens(tokens != null ? tokens.inputTokens() : 0);
            logEntry.setOutputTokens(tokens != null ? tokens.outputTokens() : 0);
            logEntry.setRequestMethod(request.getMethod());
            logEntry.setRequestPath(request.getRequestURI());
            logEntry.setResponseStatus(result.statusCode());
            logEntry.setLatencyMs(result.latencyMs());
            logEntry.setIsStreaming(result.streaming());
            requestLogService.save(logEntry);
        } catch (final Exception e) {
            log.warn("Failed to log request: {}", e.getMessage());
        }
    }
}
