package com.taller.security.controller;

import com.taller.security.dto.StatusDtos.StatusRequest;
import com.taller.security.dto.StatusDtos.StatusResponse;
import com.taller.security.service.StatusCatalogService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/admin/estatus")
@PreAuthorize("hasRole('GERENTE')")
public class StatusCatalogController {
    private final StatusCatalogService statusCatalogService;

    public StatusCatalogController(StatusCatalogService statusCatalogService) {
        this.statusCatalogService = statusCatalogService;
    }

    @GetMapping
    public List<StatusResponse> listStatuses() {
        return statusCatalogService.listStatuses();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StatusResponse createStatus(@Valid @RequestBody StatusRequest request) {
        return statusCatalogService.createStatus(request);
    }

    @PutMapping("/{id}")
    public StatusResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return statusCatalogService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    public StatusResponse cancelStatus(@PathVariable Long id) {
        return statusCatalogService.cancelStatus(id);
    }
}
