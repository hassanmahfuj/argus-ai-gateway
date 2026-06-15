package uk.mahfuj.argus.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.mahfuj.argus.entity.ModelAliasEntity;
import uk.mahfuj.argus.entity.ModelEntity;
import uk.mahfuj.argus.entity.ProviderEntity;
import uk.mahfuj.argus.repository.ModelAliasRepository;
import uk.mahfuj.argus.repository.ModelRepository;
import uk.mahfuj.argus.repository.ProviderRepository;


/**
 * Read-side façade over the provider/model/alias catalog. All reads run read-only,
 * which keeps Hibernate sessions open for association traversal. Write paths
 * (admin CRUD, seeder) use the repositories directly under their own transactions.
 */
@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final ProviderRepository providerRepository;
    private final ModelRepository modelRepository;
    private final ModelAliasRepository aliasRepository;

    public CatalogService(final ProviderRepository providerRepository,
                          final ModelRepository modelRepository,
                          final ModelAliasRepository aliasRepository) {
        this.providerRepository = providerRepository;
        this.modelRepository = modelRepository;
        this.aliasRepository = aliasRepository;
    }

    public Optional<ProviderEntity> findProvider(final String name) {
        return providerRepository.findByName(name);
    }

    public Optional<ModelEntity> findModel(final ProviderEntity provider, final String name) {
        return modelRepository.findByProviderAndName(provider, name);
    }

    public Optional<ModelAliasEntity> findAlias(final String name) {
        return aliasRepository.findByName(name);
    }

    public boolean providersExist() {
        return providerRepository.count() > 0;
    }

    public List<ModelEntity> listEnabledModels() {
        return modelRepository.findByEnabledTrue();
    }

    public List<ModelAliasEntity> listEnabledAliases() {
        return aliasRepository.findByEnabledTrue();
    }
}
