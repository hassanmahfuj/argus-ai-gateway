package uk.mahfuj.argus.service.token;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import uk.mahfuj.argus.service.proxy.Provider;


/**
 * Looks up the {@link TokenUsageExtractor} for a given {@link Provider},
 * built from all extractor beans discovered by Spring.
 */
@Component
public class TokenUsageExtractors {

    private final Map<Provider, TokenUsageExtractor> byProvider;

    public TokenUsageExtractors(final List<TokenUsageExtractor> extractors) {
        final Map<Provider, TokenUsageExtractor> map = new EnumMap<>(Provider.class);
        for (final TokenUsageExtractor extractor : extractors) {
            map.put(extractor.provider(), extractor);
        }
        this.byProvider = map;
    }

    public TokenUsageExtractor forProvider(final Provider provider) {
        return byProvider.get(provider);
    }
}
