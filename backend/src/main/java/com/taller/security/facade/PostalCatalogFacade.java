package com.taller.security.facade;

import com.taller.security.dto.PostalCatalogDtos.PostalCodeLookupResponse;
import com.taller.security.dto.PostalCatalogDtos.PostalSelectionResponse;
import com.taller.security.dto.PostalCatalogDtos.SettlementOption;
import com.taller.security.service.PostalCatalogService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PostalCatalogFacade {
    private final PostalCatalogService postalCatalogService;

    public PostalCatalogFacade(PostalCatalogService postalCatalogService) {
        this.postalCatalogService = postalCatalogService;
    }

    /**
     * Expone el catalogo postal local sin acoplar el controlador al repository.
     */
    public PostalCodeLookupResponse lookupPostalCode(String postalCode) {
        return postalCatalogService.lookupPostalCode(postalCode);
    }

    public List<String> listStates() {
        return postalCatalogService.listStates();
    }

    public List<String> listMunicipalities(String state) {
        return postalCatalogService.listMunicipalities(state);
    }

    public List<SettlementOption> listSettlements(String state, String municipality) {
        return postalCatalogService.listSettlements(state, municipality);
    }

    public PostalSelectionResponse lookupSelection(String state, String municipality, String settlement) {
        return postalCatalogService.lookupSelection(state, municipality, settlement);
    }
}
