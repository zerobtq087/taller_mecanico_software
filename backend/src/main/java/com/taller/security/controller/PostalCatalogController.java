package com.taller.security.controller;

import com.taller.security.dto.PostalCatalogDtos.PostalCodeLookupResponse;
import com.taller.security.facade.PostalCatalogFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalogos/codigos-postales")
public class PostalCatalogController {
    private final PostalCatalogFacade postalCatalogFacade;

    public PostalCatalogController(PostalCatalogFacade postalCatalogFacade) {
        this.postalCatalogFacade = postalCatalogFacade;
    }

    /**
     * Consulta colonias, municipio y estado desde el catalogo SEPOMEX cargado en MySQL local.
     */
    @GetMapping("/{postalCode}")
    public PostalCodeLookupResponse lookupPostalCode(@PathVariable String postalCode) {
        return postalCatalogFacade.lookupPostalCode(postalCode);
    }
}
