package com.espetinho.api.auth.dto;

import com.espetinho.api.user.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

@Schema(description = "Dados basicos do usuario autenticado")
public record AuthenticatedUserResponse(
        UUID id,
        String name,
        String email,
        Set<UserRole> roles
) {
}
