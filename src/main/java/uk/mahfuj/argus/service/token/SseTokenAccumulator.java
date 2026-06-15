package uk.mahfuj.argus.service.token;

/**
 * Per-provider accumulator that ingests SSE lines as they are streamed to the
 * client and produces the final {@link TokenUsage} once the stream completes.
 */
public interface SseTokenAccumulator {

    void processLine(String line);

    TokenUsage toTokenUsage();
}
