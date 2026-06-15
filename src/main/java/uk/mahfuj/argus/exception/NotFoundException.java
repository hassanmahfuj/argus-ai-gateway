package uk.mahfuj.argus.exception;

/**
 * Thrown by admin CRUD operations when a referenced entity does not exist.
 * Maps to HTTP 404.
 */
public class NotFoundException extends ArgusException {

    public NotFoundException(final String message) {
        super(404, "NOT_FOUND", message);
    }
}
