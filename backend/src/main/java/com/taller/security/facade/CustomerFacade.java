package com.taller.security.facade;

import com.taller.security.dto.CustomerDtos.CustomerRequest;
import com.taller.security.dto.CustomerDtos.CustomerResponse;
import com.taller.security.dto.CustomerDtos.CustomerUpdateRequest;
import com.taller.security.service.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class CustomerFacade {
    private final CustomerService customerService;

    public CustomerFacade(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Orquesta el alta desde la vista hacia el servicio de dominio sin exponer el repository.
     */
    public CustomerResponse registerCustomer(CustomerRequest request, String createdByEmail, MultipartFile photo) {
        return customerService.createCustomer(request, createdByEmail, photo);
    }

    /**
     * Entrega clientes paginados y filtrables sin exponer la capa repository a la vista.
     */
    public Page<CustomerResponse> listCustomers(Long workshopId, Pageable pageable) {
        return customerService.listCustomers(workshopId, pageable);
    }

    /**
     * Actualiza los datos del cliente y sus talleres asociados desde un contrato controlado.
     */
    public CustomerResponse updateCustomer(Long id, CustomerUpdateRequest request, String updatedByEmail, MultipartFile photo) {
        return customerService.updateCustomer(id, request, updatedByEmail, photo);
    }

    /**
     * Suspende el cliente de forma global para todos los talleres.
     */
    public CustomerResponse suspendCustomer(Long id) {
        return customerService.suspendCustomer(id);
    }
}
