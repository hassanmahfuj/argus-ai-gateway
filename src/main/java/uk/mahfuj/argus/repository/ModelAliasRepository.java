package uk.mahfuj.argus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.mahfuj.argus.entity.ModelAliasEntity;


public interface ModelAliasRepository extends JpaRepository<ModelAliasEntity, Long> {

    Optional<ModelAliasEntity> findByName(String name);

    boolean existsByName(String name);

    List<ModelAliasEntity> findByEnabledTrue();
}
