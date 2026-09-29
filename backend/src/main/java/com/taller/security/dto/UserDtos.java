package com.taller.security.dto;

import com.taller.security.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public final class UserDtos {
    private UserDtos() {
    }

    public record UpdateRolesRequest(@NotEmpty Set<Role> roles) {
    }

    public record CreateUserRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Email @Size(max = 180) String email,
            @NotBlank @Size(min = 8, max = 120) String password,
            @NotEmpty Set<Role> roles
    ) {
    }
}
