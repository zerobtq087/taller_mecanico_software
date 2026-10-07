package com.taller.security.service;

import com.taller.security.dto.WorkshopDtos.WorkshopRequest;
import com.taller.security.dto.WorkshopDtos.WorkshopResponse;
import com.taller.security.model.Workshop;
import com.taller.security.repository.WorkshopRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class WorkshopService {
    private final WorkshopRepository workshopRepository;
    private final FileStorageService fileStorageService;

    public WorkshopService(WorkshopRepository workshopRepository, FileStorageService fileStorageService) {
        this.workshopRepository = workshopRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public WorkshopResponse createWorkshop(WorkshopRequest request, MultipartFile photo) {
        String rfc = normalizeUpper(request.rfc());
        if (workshopRepository.existsByRfc(rfc)) {
            throw new IllegalArgumentException("Ya existe un taller registrado con ese RFC");
        }
        Workshop workshop = new Workshop();
        applyRequest(workshop, request);
        workshop.setPhotoPath(fileStorageService.saveWorkshopPhoto(photo));
        return toResponse(workshopRepository.save(workshop));
    }

    @Transactional
    public WorkshopResponse updateWorkshop(Long id, WorkshopRequest request, MultipartFile photo) {
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Taller no encontrado"));
        String rfc = normalizeUpper(request.rfc());
        workshopRepository.findByRfc(rfc)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Ya existe un taller registrado con ese RFC");
                });
        applyRequest(workshop, request);
        String photoPath = fileStorageService.saveWorkshopPhoto(photo);
        if (photoPath != null) {
            workshop.setPhotoPath(photoPath);
        }
        return toResponse(workshop);
    }

    @Transactional(readOnly = true)
    public List<WorkshopResponse> listWorkshops() {
        return workshopRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Workshop getRequiredWorkshop(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El taller es obligatorio");
        }
        return workshopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Taller no encontrado"));
    }

    private void applyRequest(Workshop workshop, WorkshopRequest request) {
        workshop.setName(normalizeText(request.name()));
        workshop.setLegalName(normalizeText(request.legalName()));
        workshop.setRfc(normalizeUpper(request.rfc()));
        workshop.setPhone(request.phone().trim());
        workshop.setEmail(normalizeEmail(request.email()));
        workshop.setStreet(normalizeText(request.street()));
        workshop.setNeighborhood(normalizeText(request.neighborhood()));
        workshop.setMunicipality(normalizeText(request.municipality()));
        workshop.setState(normalizeText(request.state()));
        workshop.setPostalCode(request.postalCode().trim());
    }

    private WorkshopResponse toResponse(Workshop workshop) {
        return new WorkshopResponse(
                workshop.getId(),
                workshop.getName(),
                workshop.getLegalName(),
                workshop.getRfc(),
                workshop.getPhone(),
                workshop.getEmail(),
                workshop.getStreet(),
                workshop.getNeighborhood(),
                workshop.getMunicipality(),
                workshop.getState(),
                workshop.getPostalCode(),
                workshop.getPhotoPath(),
                workshop.getCreatedAt()
        );
    }

    private String normalizeText(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private String normalizeEmail(String value) {
        return value.trim().toLowerCase();
    }

    private String normalizeUpper(String value) {
        return value.trim().replaceAll("\\s+", "").toUpperCase();
    }
}
