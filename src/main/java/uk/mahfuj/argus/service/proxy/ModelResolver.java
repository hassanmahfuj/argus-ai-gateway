package uk.mahfuj.argus.service.proxy;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.mahfuj.argus.entity.ModelAliasEntity;
import uk.mahfuj.argus.entity.ModelEntity;
import uk.mahfuj.argus.entity.ProviderEntity;
import uk.mahfuj.argus.exception.ResolutionException;
import uk.mahfuj.argus.service.CatalogService;
import uk.mahfuj.argus.service.CryptoService;


/**
 * Resolves a client's {@code model} string against the catalog into a forwardable
 * {@link ResolvedTarget}. Resolution rules:
 * <ul>
 *   <li>contains {@code '/'} → explicit {@code provider/model} lookup;</li>
 *   <li>otherwise → {@code model_alias} lookup;</li>
 * </ul>
 * The two namespaces cannot collide. Disabled providers/models/aliases are treated
 * as not-found (HTTP 404). A provider that has no base URL for the inbound endpoint
 * shape is rejected as unsupported (HTTP 400) — there is no shape translation.
 */
@Service
@Transactional(readOnly = true)
public class ModelResolver {

    private static final int NOT_FOUND = 404;
    private static final int BAD_REQUEST = 400;

    private final CatalogService catalog;
    private final CryptoService crypto;

    public ModelResolver(final CatalogService catalog, final CryptoService crypto) {
        this.catalog = catalog;
        this.crypto = crypto;
    }

    public ResolvedTarget resolve(final ApiShape shape, final String clientModel) {
        if (clientModel == null || clientModel.isBlank()) {
            throw new ResolutionException(NOT_FOUND, "model_not_found", "No 'model' provided in request body");
        }
        final String requested = clientModel.trim();
        final int slash = requested.indexOf('/');

        final ProviderEntity provider;
        final ModelEntity model;

        if (slash > 0) {
            final String providerName = requested.substring(0, slash);
            final String modelName = requested.substring(slash + 1);
            provider = catalog.findProvider(providerName)
                    .filter(ProviderEntity::isEnabled)
                    .orElseThrow(() -> notFound(requested));
            model = catalog.findModel(provider, modelName)
                    .filter(ModelEntity::isEnabled)
                    .orElseThrow(() -> notFound(requested));
        } else if (slash < 0) {
            final ModelAliasEntity alias = catalog.findAlias(requested)
                    .filter(ModelAliasEntity::isEnabled)
                    .orElseThrow(() -> notFound(requested));
            model = alias.getModel();
            if (model == null || !model.isEnabled()) {
                throw notFound(requested);
            }
            provider = model.getProvider();
            if (provider == null || !provider.isEnabled()) {
                throw notFound(requested);
            }
        } else {
            // Leading slash with no provider segment — not a valid model id.
            throw notFound(requested);
        }

        final String baseUrl = shape == ApiShape.OPENAI
                ? provider.getOpenaiBaseUrl()
                : provider.getAnthropicBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new ResolutionException(BAD_REQUEST, "shape_not_supported",
                    "Provider '" + provider.getName() + "' does not support the "
                            + shape.name().toLowerCase() + " endpoint");
        }

        return new ResolvedTarget(
                provider.getName(),
                shape,
                baseUrl.replaceAll("/+$", ""),
                crypto.decrypt(provider.getApiKey()),
                model.effectiveUpstreamModel(),
                requested
        );
    }

    private static ResolutionException notFound(final String requested) {
        return new ResolutionException(NOT_FOUND, "model_not_found", "Model not found: " + requested);
    }
}
