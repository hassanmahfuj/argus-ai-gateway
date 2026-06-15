package uk.mahfuj.argus.service.token;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import uk.mahfuj.argus.service.proxy.ApiShape;


/**
 * Looks up the {@link TokenUsageExtractor} for a given {@link ApiShape}, built from
 * all extractor beans discovered by Spring.
 */
@Component
public class TokenUsageExtractors {

    private final Map<ApiShape, TokenUsageExtractor> byShape;

    public TokenUsageExtractors(final List<TokenUsageExtractor> extractors) {
        final Map<ApiShape, TokenUsageExtractor> map = new EnumMap<>(ApiShape.class);
        for (final TokenUsageExtractor extractor : extractors) {
            map.put(extractor.shape(), extractor);
        }
        this.byShape = map;
    }

    public TokenUsageExtractor forShape(final ApiShape shape) {
        return byShape.get(shape);
    }
}
