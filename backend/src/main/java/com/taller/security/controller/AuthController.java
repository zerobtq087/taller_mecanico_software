package com.taller.security.controller;

import com.taller.security.dto.AuthDtos;
import com.taller.security.facade.AuthFacade;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthFacade authFacade;

    public AuthController(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDtos.AuthResponse> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return ResponseEntity.ok(authFacade.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthDtos.ResetTokenResponse> forgotPassword(
            @Valid @RequestBody AuthDtos.ForgotPasswordRequest request
    ) {
        return ResponseEntity.ok(authFacade.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody AuthDtos.ResetPasswordRequest request) {
        authFacade.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Contrasena actualizada"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            Principal principal,
            @Valid @RequestBody AuthDtos.ChangePasswordRequest request
    ) {
        authFacade.changePassword(principal.getName(), request);
        return ResponseEntity.ok(Map.of("message", "Contrasena cambiada"));
    }
}
