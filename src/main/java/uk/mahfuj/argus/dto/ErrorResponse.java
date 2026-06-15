package uk.mahfuj.argus.dto;

/**
 * Response body for management-API ({@code /api/**}) errors.
 * Mirrors the JSON shape produced by {@code error-handling-spring-boot-starter}
 * with {@code http-status-in-json-response: true}: {@code status}, {@code code},
 * {@code message}.
 */
public record ErrorResponse(
        int status,
        String code,
        String message
) {}
