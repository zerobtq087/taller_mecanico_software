package com.taller.security.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class VehicleDtos {
    private VehicleDtos() {
    }

    public static final String VIN_REGEX = "^[A-HJ-NPR-Z0-9]{17}$";
    public static final String PLATE_REGEX = "^[A-Z0-9-]{5,10}$";

    public record VehicleRequest(
            @NotBlank @Pattern(regexp = VIN_REGEX) String vin,
            @NotBlank @Pattern(regexp = PLATE_REGEX) String plate,
            @NotBlank @Size(max = 120) String make,
            @NotBlank @Size(max = 160) String model,
            @NotNull @Min(1900) @Max(2100) Integer year,
            @NotBlank @Size(max = 260) String version,
            @NotBlank @Size(max = 80) String color,
            @Min(0) Integer mileage,
            @NotBlank @Size(max = 80) String serialNumber
    ) {
    }

    public record VehicleResponse(
            Long id,
            Long customerId,
            String vin,
            String plate,
            String make,
            String model,
            Integer year,
            String version,
            String color,
            Integer mileage,
            String serialNumber,
            Long statusId,
            String status,
            Instant createdAt
    ) {
    }

    public record VehicleMakeResponse(Long id, String name, String makeGroup) {
    }

    public record VehicleModelResponse(Long id, String name, Integer yearStart, Integer yearEnd) {
    }

    public record VehicleVersionResponse(Long id, String name) {
    }
}
