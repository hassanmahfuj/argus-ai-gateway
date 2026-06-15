package uk.mahfuj.argus.service.proxy;

/**
 * Shape-agnostic representation of an Argus-originated {@code /v1} error, built from the
 * exception that triggered it. The SDK-specific {@code type} is resolved later by
 * {@link ProxyErrorSerializer} from {@link #status()} and the endpoint {@link ApiShape}.
 *
 * @param status  HTTP status to return (404 not-found, 400 shape-mismatch, 502/504 transport)
 * @param code    Argus-internal code (e.g. {@code model_not_found}, {@code shape_not_supported},
 *                {@code upstream_error}, {@code upstream_timeout}); surfaced in OpenAI's free-form
 *                {@code code} field and folded into Anthropic's {@code message}
 * @param message human-readable detail
 */
public record ProxyError(int status, String code, String message) {}
