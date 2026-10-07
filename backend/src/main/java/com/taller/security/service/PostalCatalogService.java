package com.taller.security.service;

import com.taller.security.dto.PostalCatalogDtos.PostalCodeLookupResponse;
import com.taller.security.dto.PostalCatalogDtos.PostalSelectionResponse;
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

    /**
     * Lista estados disponibles para selects encadenados sin depender de servicios externos.
     */
    @Transactional(readOnly = true)
    public List<String> listStates() {
        return postalSettlementRepository.findDistinctStates();
    }

    /**
     * Lista municipios del estado seleccionado.
     */
    @Transactional(readOnly = true)
    public List<String> listMunicipalities(String state) {
        return postalSettlementRepository.findDistinctMunicipalitiesByState(normalizeName(state));
    }

    /**
     * Lista colonias para el par estado-municipio seleccionado.
     */
    @Transactional(readOnly = true)
    public List<SettlementOption> listSettlements(String state, String municipality) {
        return postalSettlementRepository
                .findByStateNameAndMunicipalityNameOrderBySettlementNameAsc(normalizeName(state), normalizeName(municipality))
                .stream()
                .map(this::toSettlementOption)
                .toList();
    }

    /**
     * Resuelve el codigo postal a partir de estado, municipio y colonia.
     */
    @Transactional(readOnly = true)
    public PostalSelectionResponse lookupSelection(String state, String municipality, String settlement) {
        List<PostalSettlement> matches = postalSettlementRepository
                .findByStateNameAndMunicipalityNameAndSettlementNameOrderByPostalCodeAsc(
                        normalizeName(state),
                        normalizeName(municipality),
                        normalizeName(settlement)
                );
        if (matches.isEmpty()) {
            throw new IllegalArgumentException("No se encontro codigo postal para la direccion seleccionada");
        }
        PostalSettlement first = matches.get(0);
        return new PostalSelectionResponse(
                first.getStateName(),
                first.getMunicipalityName(),
                first.getSettlementName(),
                first.getPostalCode()
        );
    }

    private SettlementOption toSettlementOption(PostalSettlement settlement) {
        return new SettlementOption(
                settlement.getId(),
                settlement.getSettlementName(),
                settlement.getSettlementType(),
                settlement.getPostalCode()
        );
    }

    private String normalizePostalCode(String postalCode) {
        String normalized = postalCode == null ? "" : postalCode.trim();
        if (!normalized.matches("^[0-9]{5}$")) {
            throw new IllegalArgumentException("El codigo postal debe contener 5 digitos.");
        }
        return normalized;
    }

    private String normalizeName(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Selecciona un valor valido del catalogo postal");
        }
        return normalized;
    }
}
