package com.taller.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class StatusDtos {
    private StatusDtos() {
    }

    public record StatusRequest(
            @NotBlank @Size(max = 40) String strValor,
            @NotBlank @Size(max = 180) String strDescripcion
    ) {
    }

    public record StatusResponse(
            Long id,
            String strValor,
            String strDescripcion
    ) {
    }
}
