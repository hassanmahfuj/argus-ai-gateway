package uk.mahfuj.argus.exception;

/**
 * Thrown by {@code ModelResolver} when a client's {@code model} string cannot be
 * resolved to a usable upstream — unknown/disabled model, provider, or alias
 * (HTTP 404), or a provider that does not support the endpoint's shape (HTTP 400).
 *
 * <p>These are {@code /v1} proxy-path errors. They are caught inside the proxy
 * orchestrator and rendered as a native per-shape error envelope by
 * {@code ProxyErrorHandler}; they never reach {@link GlobalExceptionHandler} (which
 * is scoped to {@code /api}). Reusing {@link ArgusException} only for its
 * {@code status()}/{@code code()} fields.
 */
public class ResolutionException extends ArgusException {

    public ResolutionException(final int status, final String code, final String message) {
        super(status, code, message);
    }
}
