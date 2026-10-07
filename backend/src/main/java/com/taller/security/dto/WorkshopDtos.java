package com.taller.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class WorkshopDtos {
    private WorkshopDtos() {
    }

    public record WorkshopRequest(
            @NotBlank @Size(max = 160) String name,
            @NotBlank @Size(max = 220) String legalName,
            @NotBlank @Pattern(regexp = "^([A-Z&Ñ]{3,4})(\\d{6})([A-Z0-9]{3})$") String rfc,
            @NotBlank @Pattern(regexp = "^[0-9]{10}$") String phone,
            @NotBlank @Email @Size(max = 180) String email,
            @NotBlank @Size(max = 160) String street,
            @NotBlank @Size(max = 120) String neighborhood,
            @NotBlank @Size(max = 120) String municipality,
            @NotBlank @Size(max = 120) String state,
            @NotBlank @Pattern(regexp = "^[0-9]{5}$") String postalCode
    ) {
    }

    public record WorkshopResponse(
            Long id,
            String name,
            String legalName,
            String rfc,
            String phone,
            String email,
            String street,
            String neighborhood,
            String municipality,
            String state,
            String postalCode,
            String photoPath,
            Instant createdAt
    ) {
    }
}
