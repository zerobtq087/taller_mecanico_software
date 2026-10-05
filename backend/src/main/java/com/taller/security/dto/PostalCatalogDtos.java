package com.taller.security.dto;

import java.util.List;

public final class PostalCatalogDtos {
    private PostalCatalogDtos() {
    }

    public record PostalCodeLookupResponse(
            String postalCode,
            String state,
            String municipality,
            String city,
            List<SettlementOption> settlements
    ) {
    }

    public record SettlementOption(
            Long id,
            String name,
            String type
    ) {
    }
}
