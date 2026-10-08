package com.taller.security.repository;

import com.taller.security.model.StatusCatalog;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusCatalogRepository extends JpaRepository<StatusCatalog, Long> {
    Optional<StatusCatalog> findByStrValor(String strValor);

    boolean existsByStrValor(String strValor);
}
