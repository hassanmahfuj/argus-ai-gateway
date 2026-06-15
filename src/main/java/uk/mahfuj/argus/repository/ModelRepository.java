package uk.mahfuj.argus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.mahfuj.argus.entity.ModelEntity;
import uk.mahfuj.argus.entity.ProviderEntity;


public interface ModelRepository extends JpaRepository<ModelEntity, Long> {

    Optional<ModelEntity> findByProviderAndName(ProviderEntity provider, String name);

    List<ModelEntity> findByProvider(ProviderEntity provider);

    List<ModelEntity> findByEnabledTrue();
}
