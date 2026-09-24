package com.taller.security.dto;

import com.taller.security.model.Role;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public final class UserDtos {
    private UserDtos() {
    }

    public record UpdateRolesRequest(@NotEmpty Set<Role> roles) {
    }
}
