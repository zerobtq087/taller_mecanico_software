package com.taller.security.service;

import com.taller.security.dto.VehicleDtos.VehicleMakeResponse;
import com.taller.security.dto.VehicleDtos.VehicleModelResponse;
import com.taller.security.dto.VehicleDtos.VehicleVersionResponse;
import com.taller.security.repository.VehicleMakeRepository;
import com.taller.security.repository.VehicleModelRepository;
import com.taller.security.repository.VehicleVersionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleCatalogService {
    private final VehicleMakeRepository vehicleMakeRepository;
    private final VehicleModelRepository vehicleModelRepository;
    private final VehicleVersionRepository vehicleVersionRepository;

    public VehicleCatalogService(
            VehicleMakeRepository vehicleMakeRepository,
            VehicleModelRepository vehicleModelRepository,
            VehicleVersionRepository vehicleVersionRepository
    ) {
        this.vehicleMakeRepository = vehicleMakeRepository;
        this.vehicleModelRepository = vehicleModelRepository;
        this.vehicleVersionRepository = vehicleVersionRepository;
    }

    @Transactional(readOnly = true)
    public List<VehicleMakeResponse> searchMakes(String query) {
        String safeQuery = query == null ? "" : query.trim();
        return vehicleMakeRepository.search(safeQuery).stream()
                .limit(25)
                .map(make -> new VehicleMakeResponse(make.getId(), make.getName(), make.getMakeGroup()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VehicleModelResponse> searchModels(String make, String query) {
        if (make == null || make.isBlank()) {
            throw new IllegalArgumentException("La marca es obligatoria para buscar modelos");
        }
        String safeQuery = query == null || query.isBlank() ? null : query.trim();
        return vehicleModelRepository.searchByMake(make.trim(), safeQuery).stream()
                .limit(40)
                .map(model -> new VehicleModelResponse(model.getId(), model.getName(), model.getYearStart(), model.getYearEnd()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VehicleVersionResponse> searchVersions(String make, String model, String query) {
        if (make == null || make.isBlank()) {
            throw new IllegalArgumentException("La marca es obligatoria para buscar versiones");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("El modelo es obligatorio para buscar versiones");
        }
        String safeQuery = query == null || query.isBlank() ? null : query.trim();
        return vehicleVersionRepository.searchByMakeAndModel(make.trim(), model.trim(), safeQuery).stream()
                .limit(40)
                .map(version -> new VehicleVersionResponse(version.getId(), version.getName()))
                .toList();
    }
}
