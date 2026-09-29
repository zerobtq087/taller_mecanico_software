package com.taller.security.controller;

import com.taller.security.dto.AuthDtos.RegisterRequest;
import com.taller.security.dto.AuthDtos.UserResponse;
import com.taller.security.dto.UserDtos.CreateUserRequest;
import com.taller.security.facade.AuthFacade;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secretaria/users")
public class UserRegistrationController {
    private final AuthFacade authFacade;

    public UserRegistrationController(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    /**
     * Crea usuarios nuevos cuando el actor autenticado es GERENTE o SECRETARIO.
     */
    @PostMapping
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request, Authentication authentication) {
        return authFacade.createUser(
                new RegisterRequest(request.name(), request.email(), request.password()),
                request.roles(),
                authentication
        );
    }
}
