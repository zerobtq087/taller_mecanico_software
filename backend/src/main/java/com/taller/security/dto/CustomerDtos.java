package com.taller.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;

public final class CustomerDtos {
    private CustomerDtos() {
    }

    public record CustomerRequest(
            @NotBlank @Size(max = 160) String fullName,
            @NotBlank @Size(max = 160) String alternateContactName,
            @NotNull @Min(18) @Max(120) Integer age,
            @NotNull @Past LocalDate birthDate,
            @NotBlank @Pattern(regexp = "^[0-9+()\\-\\s]{8,25}$") String personalPhone,
            @NotBlank @Pattern(regexp = "^[0-9+()\\-\\s]{8,25}$") String workPhone,
            @NotBlank @Email @Size(max = 180) String email,
            @Email @Size(max = 180) String workEmail,
            @Size(max = 7000000) String photoDataUrl,
            @NotBlank @Size(max = 160) String street,
            @NotBlank @Size(max = 120) String neighborhood,
            @NotBlank @Size(max = 120) String municipality,
            @NotBlank @Size(max = 120) String state,
            @NotBlank @Pattern(regexp = "^[0-9A-Za-z\\-\\s]{4,12}$") String postalCode,
            Long workshopId
    ) {
    }

    public record CustomerResponse(
            Long id,
            String fullName,
            String alternateContactName,
            Integer age,
            LocalDate birthDate,
            String personalPhone,
            String workPhone,
            String email,
            String workEmail,
            String photoDataUrl,
            String street,
            String neighborhood,
            String municipality,
            String state,
            String postalCode,
            Long workshopId,
            Long createdByUserId,
            Instant createdAt
    ) {
    }
}
