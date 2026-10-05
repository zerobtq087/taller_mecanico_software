package com.taller.security.service;

import com.taller.security.dto.PostalCatalogDtos.PostalCodeLookupResponse;
import com.taller.security.dto.PostalCatalogDtos.SettlementOption;
import com.taller.security.model.PostalSettlement;
import com.taller.security.repository.PostalSettlementRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostalCatalogService {
    private final PostalSettlementRepository postalSettlementRepository;

    public PostalCatalogService(PostalSettlementRepository postalSettlementRepository) {
        this.postalSettlementRepository = postalSettlementRepository;
    }

    /**
     * Busca un codigo postal en el catalogo local SEPOMEX sin depender de internet.
     */
    @Transactional(readOnly = true)
    public PostalCodeLookupResponse lookupPostalCode(String postalCode) {
        String normalizedPostalCode = normalizePostalCode(postalCode);
        List<PostalSettlement> settlements = postalSettlementRepository
                .findByPostalCodeOrderBySettlementNameAsc(normalizedPostalCode);
        if (settlements.isEmpty()) {
            throw new IllegalArgumentException("No se encontro informacion local para ese codigo postal");
        }

        PostalSettlement first = settlements.get(0);
        return new PostalCodeLookupResponse(
                first.getPostalCode(),
                first.getStateName(),
                first.getMunicipalityName(),
                first.getCityName(),
                settlements.stream()
                        .map(this::toSettlementOption)
                        .toList()
        );
    }

    private SettlementOption toSettlementOption(PostalSettlement settlement) {
        return new SettlementOption(
                settlement.getId(),
                settlement.getSettlementName(),
                settlement.getSettlementType()
        );
    }

    private String normalizePostalCode(String postalCode) {
        String normalized = postalCode == null ? "" : postalCode.trim();
        if (!normalized.matches("^[0-9]{5}$")) {
            throw new IllegalArgumentException("El codigo postal debe contener 5 digitos.");
        }
        return normalized;
    }
}
