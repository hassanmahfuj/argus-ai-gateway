package uk.mahfuj.argus.dto;

/**
 * Admin write payload for a model. {@code providerId} is required on create. Fields
 * that are null on update are left unchanged.
 */
public record ModelRequest(
        Long providerId,
        String name,
        String upstreamModelName,
        Boolean enabled
) {}
