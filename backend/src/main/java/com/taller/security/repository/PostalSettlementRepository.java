package com.taller.security.repository;

import com.taller.security.model.PostalSettlement;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface PostalSettlementRepository extends JpaRepository<PostalSettlement, Long> {
    List<PostalSettlement> findByPostalCodeOrderBySettlementNameAsc(String postalCode);

    @Query("select distinct p.stateName from PostalSettlement p order by p.stateName")
    List<String> findDistinctStates();

    @Query("""
            select distinct p.municipalityName from PostalSettlement p
            where p.stateName = :state
            order by p.municipalityName
            """)
    List<String> findDistinctMunicipalitiesByState(@Param("state") String state);

    List<PostalSettlement> findByStateNameAndMunicipalityNameOrderBySettlementNameAsc(String stateName, String municipalityName);

    List<PostalSettlement> findByStateNameAndMunicipalityNameAndSettlementNameOrderByPostalCodeAsc(
            String stateName,
            String municipalityName,
            String settlementName
    );
}
