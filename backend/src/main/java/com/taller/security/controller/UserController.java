package com.taller.security.controller;

import com.taller.security.dto.AuthDtos.UserResponse;
import com.taller.security.dto.UserDtos.UpdateRolesRequest;
import com.taller.security.facade.AuthFacade;
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
    private final AuthFacade authFacade;

    public UserController(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    @GetMapping
    public List<UserResponse> listUsers() {
        return authFacade.listUsers();
    }

    @PatchMapping("/{id}/roles")
    public UserResponse updateRoles(@PathVariable Long id, @Valid @RequestBody UpdateRolesRequest request) {
        return authFacade.updateRoles(id, request.roles());
    }
}
