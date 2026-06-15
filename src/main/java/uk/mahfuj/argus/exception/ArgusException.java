package uk.mahfuj.argus.exception;

/**
 * Base class for management-API ({@code /api/**}) domain exceptions.
 * Carries the HTTP status and error code that {@link GlobalExceptionHandler}
 * maps onto the {@link uk.mahfuj.argus.dto.ErrorResponse} envelope.
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
