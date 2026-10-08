package com.taller.security.service;

import com.taller.security.dto.StatusDtos.StatusRequest;
import com.taller.security.dto.StatusDtos.StatusResponse;
import com.taller.security.model.StatusCatalog;
import com.taller.security.repository.StatusCatalogRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatusCatalogService {
    public static final String ACTIVO = "ACTIVO";
    public static final String SUSPENDIDO = "SUSPENDIDO";
    public static final String CANCELADO = "CANCELADO";

    private final StatusCatalogRepository statusCatalogRepository;

    public StatusCatalogService(StatusCatalogRepository statusCatalogRepository) {
        this.statusCatalogRepository = statusCatalogRepository;
    }

    @Transactional(readOnly = true)
    public List<StatusResponse> listStatuses() {
        return statusCatalogRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public StatusResponse createStatus(StatusRequest request) {
        String value = normalizeValue(request.strValor());
        if (statusCatalogRepository.existsByStrValor(value)) {
            throw new IllegalArgumentException("Ya existe un estatus con ese valor");
        }
        StatusCatalog status = new StatusCatalog();
        status.setStrValor(value);
        status.setStrDescripcion(normalizeDescription(request.strDescripcion()));
        return toResponse(statusCatalogRepository.save(status));
    }

    @Transactional
    public StatusResponse updateStatus(Long id, StatusRequest request) {
        StatusCatalog status = statusCatalogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Estatus no encontrado"));
        String value = normalizeValue(request.strValor());
        statusCatalogRepository.findByStrValor(value)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Ya existe un estatus con ese valor");
                });
        status.setStrValor(value);
        status.setStrDescripcion(normalizeDescription(request.strDescripcion()));
        return toResponse(status);
    }

    @Transactional
    public StatusResponse cancelStatus(Long id) {
        StatusCatalog status = statusCatalogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Estatus no encontrado"));
        if (List.of(ACTIVO, SUSPENDIDO, CANCELADO).contains(status.getStrValor())) {
            throw new IllegalArgumentException("No se puede cancelar un estatus base del sistema");
        }
        status.setStrValor(status.getStrValor() + "_CANCELADO_" + id);
        status.setStrDescripcion(status.getStrDescripcion() + " (cancelado)");
        return toResponse(status);
    }

    @Transactional(readOnly = true)
    public StatusCatalog getRequired(String value) {
        return statusCatalogRepository.findByStrValor(normalizeValue(value))
                .orElseThrow(() -> new IllegalArgumentException("Estatus no configurado: " + value));
    }

    public StatusResponse toResponse(StatusCatalog status) {
        return new StatusResponse(status.getId(), status.getStrValor(), status.getStrDescripcion());
    }

    private String normalizeValue(String value) {
        return value.trim().replaceAll("\\s+", "_").toUpperCase();
    }

    private String normalizeDescription(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
