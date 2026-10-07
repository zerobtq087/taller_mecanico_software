package com.taller.security.dto;

import com.taller.security.model.CustomerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class CustomerDtos {
    private CustomerDtos() {
    }

    public static final String NAME_REGEX = "^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ\\s]+$";
    public static final String CURP_REGEX = "^[A-Z][AEIOUX][A-Z]{2}\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])[HM](AS|BC|BS|CC|CL|CM|CS|CH|DF|DG|GT|GR|HG|JC|MC|MN|MS|NT|NL|OC|PL|QT|QR|SP|SL|SR|TC|TS|TL|VZ|YN|ZS|NE)[B-DF-HJ-NP-TV-Z]{3}[A-Z0-9]\\d$";
    public static final String RFC_REGEX = "^([A-ZÑ&]{3,4})\\d{6}[A-Z0-9]{3}$";

    public record CustomerRequest(
            @NotBlank @Size(max = 80) @Pattern(regexp = NAME_REGEX) String firstName,
            @NotBlank @Size(max = 80) @Pattern(regexp = NAME_REGEX) String lastName,
            @NotBlank @Size(max = 80) @Pattern(regexp = NAME_REGEX) String secondLastName,
            @NotBlank @Size(max = 160) @Pattern(regexp = NAME_REGEX) String alternateContactName,
            @NotNull @PastOrPresent LocalDate birthDate,
            @NotBlank @Pattern(regexp = CURP_REGEX) String curp,
            @NotBlank @Pattern(regexp = RFC_REGEX) String rfc,
            @NotBlank @Pattern(regexp = "^[0-9]{10}$") String contactPhone,
            @NotBlank @Pattern(regexp = "^[0-9]{10}$") String workPhone,
            @NotBlank @Email @Size(max = 180) String email,
            @Email @Size(max = 180) String workEmail,
            @NotBlank @Size(max = 160) String street,
            @NotBlank @Size(max = 120) String neighborhood,
            @NotBlank @Size(max = 120) String municipality,
            @NotBlank @Size(max = 120) String state,
            @NotBlank @Pattern(regexp = "^[0-9]{5}$") String postalCode,
            @NotNull Long currentWorkshopId
    ) {
    }

    public record CustomerUpdateRequest(
            @NotBlank @Size(max = 80) @Pattern(regexp = NAME_REGEX) String firstName,
            @NotBlank @Size(max = 80) @Pattern(regexp = NAME_REGEX) String lastName,
            @NotBlank @Size(max = 80) @Pattern(regexp = NAME_REGEX) String secondLastName,
            @NotBlank @Size(max = 160) @Pattern(regexp = NAME_REGEX) String alternateContactName,
            @NotNull @PastOrPresent LocalDate birthDate,
            @NotBlank @Pattern(regexp = CURP_REGEX) String curp,
            @NotBlank @Pattern(regexp = RFC_REGEX) String rfc,
            @NotBlank @Pattern(regexp = "^[0-9]{10}$") String contactPhone,
            @NotBlank @Pattern(regexp = "^[0-9]{10}$") String workPhone,
            @NotBlank @Email @Size(max = 180) String email,
            @Email @Size(max = 180) String workEmail,
            @NotBlank @Size(max = 160) String street,
            @NotBlank @Size(max = 120) String neighborhood,
            @NotBlank @Size(max = 120) String municipality,
            @NotBlank @Size(max = 120) String state,
            @NotBlank @Pattern(regexp = "^[0-9]{5}$") String postalCode,
            @NotEmpty List<Long> workshopIds
    ) {
    }

    public record WorkshopVisitResponse(
            Long workshopId,
            String workshopName,
            Instant firstVisitAt,
            Instant lastVisitAt,
            Long registeredByUserId
    ) {
    }

    public record CustomerResponse(
            Long id,
            String firstName,
            String lastName,
            String secondLastName,
            String fullName,
            String alternateContactName,
            Integer age,
            LocalDate birthDate,
            String curp,
            String rfc,
            String contactPhone,
            String workPhone,
            String email,
            String workEmail,
            String street,
            String neighborhood,
            String municipality,
            String state,
            String postalCode,
            String photoPath,
            CustomerStatus status,
            List<WorkshopVisitResponse> workshops,
            Long createdByUserId,
            Instant createdAt,
            boolean existingCustomer
    ) {
    }
}
