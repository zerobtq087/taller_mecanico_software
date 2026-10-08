package com.taller.security.controller;

import com.taller.security.dto.VehicleDtos.VehicleMakeResponse;
import com.taller.security.dto.VehicleDtos.VehicleModelResponse;
import com.taller.security.dto.VehicleDtos.VehicleVersionResponse;
import com.taller.security.service.VehicleCatalogService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalogos/vehiculos")
public class VehicleCatalogController {
    private final VehicleCatalogService vehicleCatalogService;

    public VehicleCatalogController(VehicleCatalogService vehicleCatalogService) {
        this.vehicleCatalogService = vehicleCatalogService;
    }

    @GetMapping("/marcas")
    public List<VehicleMakeResponse> searchMakes(@RequestParam(defaultValue = "") String q) {
        return vehicleCatalogService.searchMakes(q);
    }

    @GetMapping("/modelos")
    public List<VehicleModelResponse> searchModels(@RequestParam String make, @RequestParam(defaultValue = "") String q) {
        return vehicleCatalogService.searchModels(make, q);
    }

    @GetMapping("/versiones")
    public List<VehicleVersionResponse> searchVersions(
            @RequestParam String make,
            @RequestParam String model,
            @RequestParam(defaultValue = "") String q
    ) {
        return vehicleCatalogService.searchVersions(make, model, q);
    }
}
