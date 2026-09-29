package com.taller.security.facade;

import com.taller.security.dto.CustomerDtos.CustomerRequest;
import com.taller.security.dto.CustomerDtos.CustomerResponse;
import com.taller.security.service.CustomerService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CustomerFacade {
    private final CustomerService customerService;

    public CustomerFacade(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Orquesta el alta desde la vista hacia el servicio de dominio sin exponer el repository.
     */
    public CustomerResponse registerCustomer(CustomerRequest request, String createdByEmail) {
        return customerService.createCustomer(request, createdByEmail);
    }

    /**
     * Entrega clientes normalizados para consumo de Vue y futuros modulos de taller.
     */
    public List<CustomerResponse> listCustomers() {
        return customerService.listCustomers();
    }
}
