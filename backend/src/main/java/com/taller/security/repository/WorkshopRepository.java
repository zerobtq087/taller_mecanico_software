package com.taller.security.repository;

import com.taller.security.model.Workshop;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkshopRepository extends JpaRepository<Workshop, Long> {
    boolean existsByRfc(String rfc);

    Optional<Workshop> findByRfc(String rfc);
}
