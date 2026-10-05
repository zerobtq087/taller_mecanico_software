package com.taller.security.facade;

import com.taller.security.dto.PostalCatalogDtos.PostalCodeLookupResponse;
import com.taller.security.service.PostalCatalogService;
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
}
