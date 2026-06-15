package uk.mahfuj.argus.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import uk.mahfuj.argus.dto.ErrorResponse;


/**
 * Handles the {@link ArgusException} hierarchy for the management API ({@code /api/**}),
 * returning the same JSON shape that {@code error-handling-spring-boot-starter} emits
 * by default (status / code / message). Only {@code ArgusException} subclasses are
 * claimed here; every other exception continues to be handled by the starter, so the
 * existing contract for non-domain errors is preserved.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ArgusException.class)
    public ResponseEntity<ErrorResponse> handleArgusException(final ArgusException ex) {
        return ResponseEntity
                .status(ex.status())
                .body(new ErrorResponse(ex.status(), ex.code(), ex.getMessage()));
    }
}
