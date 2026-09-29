package com.taller.security.facade;

import com.taller.security.dto.AuthDtos;
import com.taller.security.dto.AuthDtos.UserResponse;
import com.taller.security.model.Role;
import com.taller.security.service.AuthService;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthFacade {
    private final AuthService authService;

    public AuthFacade(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Centraliza el login solicitado por Vue y devuelve el JWT con los datos del usuario.
     */
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        return authService.login(request);
    }

    /**
     * Orquesta el alta protegida de usuarios desde gerencia o recepcion.
     */
    public UserResponse createUser(AuthDtos.RegisterRequest request, Set<Role> roles, Authentication actor) {
        return authService.createUser(request, roles, actor);
    }

    /**
     * Inicia la recuperacion de contrasena sin exponer detalles del servicio a la capa REST.
     */
    public AuthDtos.ResetTokenResponse forgotPassword(AuthDtos.ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    /**
     * Completa el cambio de contrasena por token de recuperacion.
     */
    public void resetPassword(AuthDtos.ResetPasswordRequest request) {
        authService.resetPassword(request);
    }

    /**
     * Cambia la contrasena de un usuario autenticado validando la contrasena actual.
     */
    public void changePassword(String email, AuthDtos.ChangePasswordRequest request) {
        authService.changePassword(email, request);
    }
}
