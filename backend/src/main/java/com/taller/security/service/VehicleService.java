package com.taller.security.service;

import com.taller.security.dto.VehicleDtos.VehicleRequest;
import com.taller.security.dto.VehicleDtos.VehicleResponse;
import com.taller.security.model.Customer;
import com.taller.security.model.StatusCatalog;
import com.taller.security.model.Vehicle;
import com.taller.security.repository.CustomerRepository;
import com.taller.security.repository.VehicleRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final StatusCatalogService statusCatalogService;

    public VehicleService(
            VehicleRepository vehicleRepository,
            CustomerRepository customerRepository,
            StatusCatalogService statusCatalogService
    ) {
        this.vehicleRepository = vehicleRepository;
        this.customerRepository = customerRepository;
        this.statusCatalogService = statusCatalogService;
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> listVehicles(Long customerId) {
        return vehicleRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public VehicleResponse createVehicle(Long customerId, VehicleRequest request) {
        Customer customer = getActiveCustomer(customerId);
        String vin = normalizeUpper(request.vin());
        String plate = normalizePlate(request.plate());
        if (vehicleRepository.existsByVin(vin)) {
            throw new IllegalArgumentException("Ya existe un vehiculo registrado con ese VIN");
        }
        if (vehicleRepository.existsByPlate(plate)) {
            throw new IllegalArgumentException("Ya existe un vehiculo registrado con esas placas");
        }
        Vehicle vehicle = new Vehicle();
        vehicle.setCustomer(customer);
        applyRequest(vehicle, request);
        vehicle.setStatus(statusCatalogService.getRequired(StatusCatalogService.ACTIVO));
        return toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional
    public VehicleResponse updateVehicle(Long customerId, Long vehicleId, VehicleRequest request) {
        getActiveCustomer(customerId);
        Vehicle vehicle = vehicleRepository.findByIdAndCustomerId(vehicleId, customerId)
                .orElseThrow(() -> new IllegalArgumentException("Vehiculo no encontrado para el cliente"));
        String vin = normalizeUpper(request.vin());
        String plate = normalizePlate(request.plate());
        vehicleRepository.findByVin(vin)
                .filter(existing -> !existing.getId().equals(vehicleId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Ya existe otro vehiculo registrado con ese VIN");
                });
        vehicleRepository.findByPlate(plate)
                .filter(existing -> !existing.getId().equals(vehicleId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Ya existe otro vehiculo registrado con esas placas");
                });
        applyRequest(vehicle, request);
        return toResponse(vehicle);
    }

    @Transactional
    public VehicleResponse cancelVehicle(Long customerId, Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findByIdAndCustomerId(vehicleId, customerId)
                .orElseThrow(() -> new IllegalArgumentException("Vehiculo no encontrado para el cliente"));
        vehicle.setStatus(statusCatalogService.getRequired(StatusCatalogService.CANCELADO));
        return toResponse(vehicle);
    }

    private Customer getActiveCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        if (!StatusCatalogService.ACTIVO.equals(customer.getStatus().getStrValor())) {
            throw new IllegalArgumentException("Solo se pueden agregar vehiculos a clientes activos");
        }
        return customer;
    }

    private void applyRequest(Vehicle vehicle, VehicleRequest request) {
        vehicle.setVin(normalizeUpper(request.vin()));
        vehicle.setPlate(normalizePlate(request.plate()));
        vehicle.setMake(normalizeText(request.make()));
        vehicle.setModel(normalizeText(request.model()));
        vehicle.setYear(request.year());
        vehicle.setVersion(normalizeText(request.version()));
        vehicle.setColor(normalizeText(request.color()));
        vehicle.setMileage(request.mileage());
        vehicle.setSerialNumber(normalizeUpper(request.serialNumber()));
    }

    private VehicleResponse toResponse(Vehicle vehicle) {
        StatusCatalog status = vehicle.getStatus();
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getCustomer().getId(),
                vehicle.getVin(),
                vehicle.getPlate(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getVersion(),
                vehicle.getColor(),
                vehicle.getMileage(),
                vehicle.getSerialNumber(),
                status.getId(),
                status.getStrValor(),
                vehicle.getCreatedAt()
        );
    }

    private String normalizeText(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private String normalizeUpper(String value) {
        return value.trim().replaceAll("\\s+", "").toUpperCase();
    }

    private String normalizePlate(String value) {
        return value.trim().replaceAll("\\s+", "").toUpperCase();
    }
}
