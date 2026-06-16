package com.espetinho.api.auth.dto;

import com.espetinho.api.user.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Resposta do cadastro tradicional")
public record RegisterResponse(
        UUID id,
        String name,
        String email,
        UserRole role,
        boolean emailVerified
) {
}
