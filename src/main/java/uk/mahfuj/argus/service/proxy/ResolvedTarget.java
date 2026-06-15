package uk.mahfuj.argus.service.proxy;

/**
 * The fully-resolved destination for a proxied request: everything the executor needs
 * to forward — the upstream provider name (for logging), the endpoint shape, the base
 * URL to forward to, the decrypted API key, the model name to rewrite into the
 * request body, and the model string the client originally sent.
 *
 * @param providerName    catalog name of the upstream provider (e.g. {@code zai})
 * @param shape           wire shape of the endpoint the request arrived on
 * @param baseUrl         provider base URL for this shape (prefix already trimmed off)
 * @param apiKey          decrypted upstream key — injected as a Bearer token
 * @param upstreamModel   model name to write into the forwarded body
 * @param requestedModel  the model/alias string exactly as the client sent it
 */
public record ResolvedTarget(
        String providerName,
        ApiShape shape,
        String baseUrl,
        String apiKey,
        String upstreamModel,
        String requestedModel
) {}
