package uk.mahfuj.argus.service.proxy;

/**
 * The wire-protocol shape of a request/response, derived from which endpoint the
 * client hit ({@code /v1/**} → {@link #OPENAI}, {@code /v1/anthropic/**} →
 * {@link #ANTHROPIC}). Distinct from a catalog {@code ProviderEntity} (the upstream
 * host): many hosts share one shape (zai, xai, deepseek are all OpenAI-shaped), and
 * a single host may support both shapes. Token-usage extraction is keyed on shape,
 * not on the upstream host.
 */
public enum ApiShape {
    OPENAI,
    ANTHROPIC
}
