package com.taller.security.controller;

import com.taller.security.dto.WorkshopDtos.WorkshopRequest;
import com.taller.security.dto.WorkshopDtos.WorkshopResponse;
import com.taller.security.facade.WorkshopFacade;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/talleres")
public class WorkshopController {
    private final WorkshopFacade workshopFacade;

    public WorkshopController(WorkshopFacade workshopFacade) {
        this.workshopFacade = workshopFacade;
    }

    /**
     * Lista talleres disponibles para asociar clientes e historial operativo.
     */
    @GetMapping
    public List<WorkshopResponse> listWorkshops() {
        return workshopFacade.listWorkshops();
    }

    /**
     * Crea una sucursal con foto opcional; solo GERENTE administra talleres.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('GERENTE')")
    public WorkshopResponse createWorkshop(
            @Valid @RequestPart("request") WorkshopRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo
    ) {
        return workshopFacade.createWorkshop(request, photo);
    }

    /**
     * Actualiza datos y puede reemplazar la foto si se envia un nuevo archivo.
     */
    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('GERENTE')")
    public WorkshopResponse updateWorkshop(
            @PathVariable Long id,
            @Valid @RequestPart("request") WorkshopRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo
    ) {
        return workshopFacade.updateWorkshop(id, request, photo);
    }
}
