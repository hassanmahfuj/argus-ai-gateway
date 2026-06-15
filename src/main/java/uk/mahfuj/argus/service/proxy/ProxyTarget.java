package uk.mahfuj.argus.service.proxy;

/**
 * Resolved destination for a proxied request: the target {@link Provider} and
 * the fully-built upstream URL (including query string).
 */
public record ProxyTarget(Provider provider, String targetUrl) {}
