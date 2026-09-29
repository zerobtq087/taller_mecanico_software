package com.taller.security.controller;

import com.taller.security.dto.CustomerDtos.CustomerRequest;
import com.taller.security.dto.CustomerDtos.CustomerResponse;
import com.taller.security.facade.CustomerFacade;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse registerCustomer(
            Principal principal,
            @Valid @RequestBody CustomerRequest request
    ) {
        return customerFacade.registerCustomer(request, principal.getName());
    }

    /**
     * Consulta clientes para validar registros previos desde la vista operativa.
     */
    @GetMapping
    public List<CustomerResponse> listCustomers() {
        return customerFacade.listCustomers();
    }
}
