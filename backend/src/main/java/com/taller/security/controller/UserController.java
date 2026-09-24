package com.taller.security.controller;

import com.taller.security.dto.AuthDtos.UserResponse;
import com.taller.security.dto.UserDtos.UpdateRolesRequest;
import com.taller.security.repository.UserRepository;
import com.taller.security.service.AuthService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {
    private final UserRepository userRepository;
    private final AuthService authService;

    public UserController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @GetMapping
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(authService::toResponse).toList();
    }

    @PatchMapping("/{id}/roles")
    public UserResponse updateRoles(@PathVariable Long id, @Valid @RequestBody UpdateRolesRequest request) {
        var user = userRepository.findById(id).orElseThrow();
        user.setRoles(java.util.EnumSet.copyOf(request.roles()));
        return authService.toResponse(userRepository.save(user));
    }
}
