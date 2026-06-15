package uk.mahfuj.argus.mapper;

import uk.mahfuj.argus.dto.ModelAliasResponse;
import uk.mahfuj.argus.dto.ModelResponse;
import uk.mahfuj.argus.dto.ProviderResponse;
import uk.mahfuj.argus.entity.ModelAliasEntity;
import uk.mahfuj.argus.entity.ModelEntity;
import uk.mahfuj.argus.entity.ProviderEntity;


/**
 * Manual entity → response-DTO converters for the catalog admin API. (No MapStruct,
 * per project convention.) Provider responses never carry the API key.
 */
public final class CatalogMappers {

    private CatalogMappers() {}

    public static ProviderResponse toResponse(final ProviderEntity e) {
        return new ProviderResponse(
                e.getId(),
                e.getName(),
                e.getOpenaiBaseUrl(),
                e.getAnthropicBaseUrl(),
                e.isEnabled(),
                e.getApiKey() != null && !e.getApiKey().isBlank(),
                e.getCreatedAt(),
                e.getUpdatedAt());
    }

    public static ModelResponse toResponse(final ModelEntity e) {
        final Long providerId = e.getProvider() != null ? e.getProvider().getId() : null;
        final String providerName = e.getProvider() != null ? e.getProvider().getName() : null;
        return new ModelResponse(
                e.getId(),
                providerId,
                providerName,
                e.getName(),
                e.getUpstreamModelName(),
                e.isEnabled(),
                e.getCreatedAt(),
                e.getUpdatedAt());
    }

    public static ModelAliasResponse toResponse(final ModelAliasEntity e) {
        final Long modelId = e.getModel() != null ? e.getModel().getId() : null;
        final String modelName = e.getModel() != null ? e.getModel().getName() : null;
        return new ModelAliasResponse(
                e.getId(),
                e.getName(),
                modelId,
                modelName,
                e.isEnabled(),
                e.getCreatedAt(),
                e.getUpdatedAt());
    }
}
