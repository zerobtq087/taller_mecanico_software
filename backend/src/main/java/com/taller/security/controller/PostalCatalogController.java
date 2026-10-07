package com.taller.security.controller;

import com.taller.security.dto.PostalCatalogDtos.PostalCodeLookupResponse;
import com.taller.security.dto.PostalCatalogDtos.PostalSelectionResponse;
import com.taller.security.dto.PostalCatalogDtos.SettlementOption;
import com.taller.security.facade.PostalCatalogFacade;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/estados")
    public List<String> listStates() {
        return postalCatalogFacade.listStates();
    }

    @GetMapping("/municipios")
    public List<String> listMunicipalities(@RequestParam String state) {
        return postalCatalogFacade.listMunicipalities(state);
    }

    @GetMapping("/colonias")
    public List<SettlementOption> listSettlements(@RequestParam String state, @RequestParam String municipality) {
        return postalCatalogFacade.listSettlements(state, municipality);
    }

    @GetMapping("/buscar")
    public PostalSelectionResponse lookupSelection(
            @RequestParam String state,
            @RequestParam String municipality,
            @RequestParam String settlement
    ) {
        return postalCatalogFacade.lookupSelection(state, municipality, settlement);
    }
}
