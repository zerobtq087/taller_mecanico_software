package com.taller.security.controller;

import com.taller.security.dto.CustomerDtos.CustomerRequest;
import com.taller.security.dto.CustomerDtos.CustomerResponse;
import com.taller.security.dto.CustomerDtos.CustomerUpdateRequest;
import com.taller.security.facade.CustomerFacade;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/secretaria/clientes")
public class CustomerController {
    private final CustomerFacade customerFacade;

    public CustomerController(CustomerFacade customerFacade) {
        this.customerFacade = customerFacade;
    }

    /**
     * Registra un cliente cuando el usuario autenticado tiene rol GERENTE o SECRETARIO.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse registerCustomer(
            Principal principal,
            @Valid @RequestPart("request") CustomerRequest request,
            @RequestPart("photo") MultipartFile photo
    ) {
        return customerFacade.registerCustomer(request, principal.getName(), photo);
    }

    /**
     * Consulta clientes para validar registros previos desde la vista operativa.
     */
    @GetMapping
    public Page<CustomerResponse> listCustomers(
            @RequestParam(required = false) Long workshopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Sort sort = Sort.by(resolveSortField(sortBy));
        sort = "desc".equalsIgnoreCase(direction) ? sort.descending() : sort.ascending();
        return customerFacade.listCustomers(workshopId, PageRequest.of(safePage, safeSize, sort));
    }

    /**
     * Edita los datos del cliente y permite cambiar su lista de talleres asociados.
     */
    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CustomerResponse updateCustomer(
            @PathVariable Long id,
            Principal principal,
            @Valid @RequestPart("request") CustomerUpdateRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo
    ) {
        return customerFacade.updateCustomer(id, request, principal.getName(), photo);
    }

    /**
     * Suspende un cliente de manera global; solo GERENTE puede ejecutar esta accion.
     */
    @PatchMapping("/{id}/suspender")
    @PreAuthorize("hasRole('GERENTE')")
    public CustomerResponse suspendCustomer(@PathVariable Long id) {
        return customerFacade.suspendCustomer(id);
    }

    private String resolveSortField(String requested) {
        Set<String> allowed = Set.of(
                "id", "firstName", "lastName", "secondLastName", "curp", "rfc", "email",
                "contactPhone", "status", "createdAt", "birthDate", "postalCode", "state", "municipality"
        );
        Map<String, String> aliases = Map.of(
                "fullName", "firstName",
                "age", "birthDate"
        );
        String field = aliases.getOrDefault(requested, requested);
        return allowed.contains(field) ? field : "id";
    }
}
