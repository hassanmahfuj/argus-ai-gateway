package uk.mahfuj.argus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.mahfuj.argus.entity.ProviderEntity;


public interface ProviderRepository extends JpaRepository<ProviderEntity, Long> {

    Optional<ProviderEntity> findByName(String name);

    boolean existsByName(String name);
}
