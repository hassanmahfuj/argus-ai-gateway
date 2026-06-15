package uk.mahfuj.argus.exception;

/**
 * Base for Argus domain exceptions. Carries the HTTP status and error code.
 *
 * <p>Two scopes: management-API ({@code /api/**}) subclasses like
 * {@link BadRequestException} are mapped by {@link GlobalExceptionHandler} onto the
 * {@link uk.mahfuj.argus.dto.ErrorResponse} envelope; proxy ({@code /v1}) subclasses
 * like {@link ResolutionException} are caught inside the proxy orchestrator and
 * rendered as a per-shape envelope, so they never reach that advice.
 */
public abstract class ArgusException extends RuntimeException {

    private final int status;
    private final String code;

    protected ArgusException(final int status, final String code, final String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public int status() {
        return status;
    }

    public String code() {
        return code;
    }
}
