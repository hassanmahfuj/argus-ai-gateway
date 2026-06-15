package uk.mahfuj.argus.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import uk.mahfuj.argus.entity.ModelAliasEntity;
import uk.mahfuj.argus.entity.ModelEntity;
import uk.mahfuj.argus.entity.ProviderEntity;
import uk.mahfuj.argus.repository.ModelAliasRepository;
import uk.mahfuj.argus.repository.ModelRepository;
import uk.mahfuj.argus.repository.ProviderRepository;


/**
 * Seeds the default ZAI catalog on first boot (when no provider row exists yet),
 * so a fresh deploy is immediately usable. Idempotent: once any provider exists it
 * does nothing — admin edits are never clobbered. The ZAI key is read from the
 * {@code ZAI_API_KEY} env var and AES-encrypted before storage.
 */
@Component
@Order(0)
public class CatalogSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogSeeder.class);

    private final ProviderRepository providers;
    private final ModelRepository models;
    private final ModelAliasRepository aliases;
    private final CryptoService crypto;
    private final String zaiApiKey;

    public CatalogSeeder(final ProviderRepository providers,
                         final ModelRepository models,
                         final ModelAliasRepository aliases,
                         final CryptoService crypto,
                         @Value("${ZAI_API_KEY:}") final String zaiApiKey) {
        this.providers = providers;
        this.models = models;
        this.aliases = aliases;
        this.crypto = crypto;
        this.zaiApiKey = zaiApiKey;
    }

    @Override
    @Transactional
    public void run(final String... args) {
        if (providers.count() > 0) {
            log.info("Catalog already populated; skipping seed.");
            return;
        }
        log.info("Seeding default ZAI catalog…");

        final ProviderEntity zai = new ProviderEntity();
        zai.setName("zai");
        zai.setApiKey(crypto.encrypt(zaiApiKey != null ? zaiApiKey : ""));
        zai.setOpenaiBaseUrl("https://api.z.ai/api/coding/paas/v4");
        zai.setAnthropicBaseUrl("https://api.z.ai/api/anthropic");
        zai.setEnabled(true);
        providers.save(zai);

        final ModelEntity glm46 = model(zai, "glm-4.6", null);
        final ModelEntity glm51 = model(zai, "glm-5.1", null);

        alias("flash", glm46);
        alias("pro", glm51);

        log.info("Catalog seed complete: provider 'zai', models glm-4.6/glm-5.1, aliases flash→glm-4.6, pro→glm-5.1.");
    }

    private ModelEntity model(final ProviderEntity provider, final String name, final String upstreamModelName) {
        final ModelEntity m = new ModelEntity();
        m.setProvider(provider);
        m.setName(name);
        m.setUpstreamModelName(upstreamModelName);
        m.setEnabled(true);
        return models.save(m);
    }

    private void alias(final String name, final ModelEntity target) {
        final ModelAliasEntity a = new ModelAliasEntity();
        a.setName(name);
        a.setModel(target);
        a.setEnabled(true);
        aliases.save(a);
    }
}
