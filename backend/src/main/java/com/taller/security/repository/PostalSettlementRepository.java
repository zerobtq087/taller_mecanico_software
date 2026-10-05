package com.taller.security.repository;

import com.taller.security.model.PostalSettlement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostalSettlementRepository extends JpaRepository<PostalSettlement, Long> {
    List<PostalSettlement> findByPostalCodeOrderBySettlementNameAsc(String postalCode);
}
