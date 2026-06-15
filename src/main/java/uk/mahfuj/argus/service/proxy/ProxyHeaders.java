package uk.mahfuj.argus.service.proxy;

import java.util.Set;

/**
 * Hop-by-hop / pseudo-header filtering for forwarded requests and responses.
 */
final class ProxyHeaders {

    private static final Set<String> HOP_BY_HOP = Set.of(
            "host", "connection", "transfer-encoding", "content-length"
    );

    private ProxyHeaders() {}

    static boolean shouldForward(final String name) {
        final String lower = name.toLowerCase();
        return !lower.startsWith(":") && !HOP_BY_HOP.contains(lower);
    }
}
