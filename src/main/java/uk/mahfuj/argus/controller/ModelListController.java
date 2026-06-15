package uk.mahfuj.argus.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.mahfuj.argus.dto.ModelListResponse;
import uk.mahfuj.argus.entity.ModelAliasEntity;
import uk.mahfuj.argus.entity.ModelEntity;
import uk.mahfuj.argus.service.CatalogService;


/**
 * Serves the OpenAI-style model list for the proxy endpoint. This more-specific
 * {@code GET /v1/models} mapping takes precedence over the catch-all {@code /v1/**}
 * proxy mapping. Lists enabled registered models ({@code provider/model}) and
 * enabled aliases (bare name).
 */
@RestController
public class ModelListController {

    private final CatalogService catalogService;

    public ModelListController(final CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/v1/models")
    public ModelListResponse list() {
        final List<ModelListResponse.Entry> data = new ArrayList<>();
        for (final ModelEntity model : catalogService.listEnabledModels()) {
            final String providerName = model.getProvider() != null ? model.getProvider().getName() : "";
            data.add(new ModelListResponse.Entry(
                    providerName + "/" + model.getName(),
                    "model",
                    epochSeconds(model.getCreatedAt()),
                    providerName));
        }
        for (final ModelAliasEntity alias : catalogService.listEnabledAliases()) {
            final String owner = alias.getModel() != null && alias.getModel().getProvider() != null
                    ? alias.getModel().getProvider().getName() : "";
            data.add(new ModelListResponse.Entry(
                    alias.getName(),
                    "model",
                    epochSeconds(alias.getCreatedAt()),
                    owner));
        }
        return new ModelListResponse("list", data);
    }

    private static long epochSeconds(final Instant instant) {
        return instant != null ? instant.getEpochSecond() : 0L;
    }
}
