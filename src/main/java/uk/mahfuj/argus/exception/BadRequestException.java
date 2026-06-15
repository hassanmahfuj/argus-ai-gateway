package uk.mahfuj.argus.exception;

/**
 * Thrown for invalid client input on the management API ({@code /api/**}).
 * Maps to HTTP 400.
 */
public class BadRequestException extends ArgusException {

    public BadRequestException(final String message) {
        super(400, "BAD_REQUEST", message);
    }
}
