package com.taller.security.controller;

import com.taller.security.dto.VehicleDtos.VehicleRequest;
import com.taller.security.dto.VehicleDtos.VehicleResponse;
import com.taller.security.service.VehicleService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secretaria/clientes/{customerId}/vehiculos")
public class VehicleController {
    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public List<VehicleResponse> listVehicles(@PathVariable Long customerId) {
        return vehicleService.listVehicles(customerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse createVehicle(@PathVariable Long customerId, @Valid @RequestBody VehicleRequest request) {
        return vehicleService.createVehicle(customerId, request);
    }

    @PutMapping("/{vehicleId}")
    public VehicleResponse updateVehicle(
            @PathVariable Long customerId,
            @PathVariable Long vehicleId,
            @Valid @RequestBody VehicleRequest request
    ) {
        return vehicleService.updateVehicle(customerId, vehicleId, request);
    }

    @DeleteMapping("/{vehicleId}")
    public VehicleResponse cancelVehicle(@PathVariable Long customerId, @PathVariable Long vehicleId) {
        return vehicleService.cancelVehicle(customerId, vehicleId);
    }
}
