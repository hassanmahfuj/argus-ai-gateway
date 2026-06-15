package uk.mahfuj.argus.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.mahfuj.argus.dto.ModelAliasRequest;
import uk.mahfuj.argus.dto.ModelRequest;
import uk.mahfuj.argus.dto.ProviderRequest;
import uk.mahfuj.argus.entity.ModelAliasEntity;
import uk.mahfuj.argus.entity.ModelEntity;
import uk.mahfuj.argus.entity.ProviderEntity;
import uk.mahfuj.argus.exception.BadRequestException;
import uk.mahfuj.argus.exception.NotFoundException;
import uk.mahfuj.argus.repository.ModelAliasRepository;
import uk.mahfuj.argus.repository.ModelRepository;
import uk.mahfuj.argus.repository.ProviderRepository;


/**
 * Write side of the provider/model/alias catalog (the admin CRUD API). Provider API
 * keys are encrypted before persistence; responses never reveal them. Null optional
 * fields on update mean "leave unchanged". Referential integrity (provider→models,
 * model→aliases) is enforced: deleting a referenced row rejects with a clear 400.
 */
@Service
public class CatalogAdminService {

    private final ProviderRepository providers;
    private final ModelRepository models;
    private final ModelAliasRepository aliases;
    private final CryptoService crypto;

    public CatalogAdminService(final ProviderRepository providers,
                               final ModelRepository models,
                               final ModelAliasRepository aliases,
                               final CryptoService crypto) {
        this.providers = providers;
        this.models = models;
        this.aliases = aliases;
        this.crypto = crypto;
    }

    // ---------- Provider ----------

    @Transactional
    public ProviderEntity createProvider(final ProviderRequest r) {
        require(r.name(), "name");
        require(r.apiKey(), "apiKey");
        final String name = r.name().trim();
        if (providers.existsByName(name)) {
            throw new BadRequestException("Provider already exists: " + name);
        }
        final ProviderEntity e = new ProviderEntity();
        e.setName(name);
        e.setApiKey(crypto.encrypt(r.apiKey().trim()));
        e.setOpenaiBaseUrl(trimToNull(r.openaiBaseUrl()));
        e.setAnthropicBaseUrl(trimToNull(r.anthropicBaseUrl()));
        e.setEnabled(r.enabled() != null ? r.enabled() : true);
        return providers.save(e);
    }

    @Transactional(readOnly = true)
    public List<ProviderEntity> listProviders() {
        return providers.findAll(Sort.by("name"));
    }

    @Transactional(readOnly = true)
    public ProviderEntity getProvider(final Long id) {
        return providers.findById(id)
                .orElseThrow(() -> new NotFoundException("Provider not found: " + id));
    }

    @Transactional
    public ProviderEntity updateProvider(final Long id, final ProviderRequest r) {
        final ProviderEntity e = getProvider(id);
        if (notBlank(r.name())) {
            final String name = r.name().trim();
            if (!name.equals(e.getName()) && providers.existsByName(name)) {
                throw new BadRequestException("Provider already exists: " + name);
            }
            e.setName(name);
        }
        if (notBlank(r.apiKey())) {
            e.setApiKey(crypto.encrypt(r.apiKey().trim()));
        }
        if (r.openaiBaseUrl() != null) {
            e.setOpenaiBaseUrl(trimToNull(r.openaiBaseUrl()));
        }
        if (r.anthropicBaseUrl() != null) {
            e.setAnthropicBaseUrl(trimToNull(r.anthropicBaseUrl()));
        }
        if (r.enabled() != null) {
            e.setEnabled(r.enabled());
        }
        return providers.save(e);
    }

    @Transactional
    public void deleteProvider(final Long id) {
        getProvider(id);
        try {
            providers.deleteById(id);
        } catch (final DataIntegrityViolationException ex) {
            throw new BadRequestException("Provider has models assigned; remove them first.");
        }
    }

    // ---------- Model ----------

    @Transactional
    public ModelEntity createModel(final ModelRequest r) {
        require(r.providerId(), "providerId");
        require(r.name(), "name");
        final ProviderEntity p = providers.findById(r.providerId())
                .orElseThrow(() -> new NotFoundException("Provider not found: " + r.providerId()));
        final String name = r.name().trim();
        if (models.findByProviderAndName(p, name).isPresent()) {
            throw new BadRequestException("Model already exists: " + p.getName() + "/" + name);
        }
        final ModelEntity e = new ModelEntity();
        e.setProvider(p);
        e.setName(name);
        e.setUpstreamModelName(trimToNull(r.upstreamModelName()));
        e.setEnabled(r.enabled() != null ? r.enabled() : true);
        return models.save(e);
    }

    @Transactional(readOnly = true)
    public List<ModelEntity> listModels() {
        return models.findAll(Sort.by("name"));
    }

    @Transactional(readOnly = true)
    public ModelEntity getModel(final Long id) {
        return models.findById(id)
                .orElseThrow(() -> new NotFoundException("Model not found: " + id));
    }

    @Transactional
    public ModelEntity updateModel(final Long id, final ModelRequest r) {
        final ModelEntity e = getModel(id);
        if (r.providerId() != null) {
            e.setProvider(providers.findById(r.providerId())
                    .orElseThrow(() -> new NotFoundException("Provider not found: " + r.providerId())));
        }
        if (notBlank(r.name())) {
            final String name = r.name().trim();
            final ProviderEntity p = e.getProvider();
            if (!name.equals(e.getName()) && models.findByProviderAndName(p, name).isPresent()) {
                throw new BadRequestException("Model already exists: " + p.getName() + "/" + name);
            }
            e.setName(name);
        }
        if (r.upstreamModelName() != null) {
            e.setUpstreamModelName(trimToNull(r.upstreamModelName()));
        }
        if (r.enabled() != null) {
            e.setEnabled(r.enabled());
        }
        return models.save(e);
    }

    @Transactional
    public void deleteModel(final Long id) {
        getModel(id);
        try {
            models.deleteById(id);
        } catch (final DataIntegrityViolationException ex) {
            throw new BadRequestException("Model has aliases assigned; remove them first.");
        }
    }

    // ---------- Alias ----------

    @Transactional
    public ModelAliasEntity createAlias(final ModelAliasRequest r) {
        require(r.name(), "name");
        require(r.modelId(), "modelId");
        final String name = r.name().trim();
        if (name.contains("/")) {
            throw new BadRequestException("Alias name must not contain '/': " + name);
        }
        if (aliases.existsByName(name)) {
            throw new BadRequestException("Alias already exists: " + name);
        }
        final ModelEntity m = models.findById(r.modelId())
                .orElseThrow(() -> new NotFoundException("Model not found: " + r.modelId()));
        final ModelAliasEntity e = new ModelAliasEntity();
        e.setName(name);
        e.setModel(m);
        e.setEnabled(r.enabled() != null ? r.enabled() : true);
        return aliases.save(e);
    }

    @Transactional(readOnly = true)
    public List<ModelAliasEntity> listAliases() {
        return aliases.findAll(Sort.by("name"));
    }

    @Transactional(readOnly = true)
    public ModelAliasEntity getAlias(final Long id) {
        return aliases.findById(id)
                .orElseThrow(() -> new NotFoundException("Alias not found: " + id));
    }

    @Transactional
    public ModelAliasEntity updateAlias(final Long id, final ModelAliasRequest r) {
        final ModelAliasEntity e = getAlias(id);
        if (notBlank(r.name())) {
            final String name = r.name().trim();
            if (name.contains("/")) {
                throw new BadRequestException("Alias name must not contain '/': " + name);
            }
            if (!name.equals(e.getName()) && aliases.existsByName(name)) {
                throw new BadRequestException("Alias already exists: " + name);
            }
            e.setName(name);
        }
        if (r.modelId() != null) {
            e.setModel(models.findById(r.modelId())
                    .orElseThrow(() -> new NotFoundException("Model not found: " + r.modelId())));
        }
        if (r.enabled() != null) {
            e.setEnabled(r.enabled());
        }
        return aliases.save(e);
    }

    @Transactional
    public void deleteAlias(final Long id) {
        getAlias(id);
        aliases.deleteById(id);
    }

    // ---------- helpers ----------

    private static void require(final Object value, final String field) {
        final boolean missing = value == null || (value instanceof final String s && s.isBlank());
        if (missing) {
            throw new BadRequestException("Missing required field: " + field);
        }
    }

    private static boolean notBlank(final String s) {
        return s != null && !s.isBlank();
    }

    private static String trimToNull(final String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
