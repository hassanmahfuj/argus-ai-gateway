package uk.mahfuj.argus.service.proxy;

/**
 * The upstream AI provider a proxied request is routed to.
 */
public enum Provider {

    OPENAI("openai"),
    ANTHROPIC("anthropic");

    private final String dbValue;

    Provider(final String dbValue) {
        this.dbValue = dbValue;
    }

    /** Value persisted to the {@code api_request_log.provider} column. */
    public String dbValue() {
        return dbValue;
    }
}
