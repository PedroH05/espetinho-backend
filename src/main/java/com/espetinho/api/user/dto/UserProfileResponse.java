package com.espetinho.api.user.dto;

import com.espetinho.api.user.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Dados do usuario autenticado")
public record UserProfileResponse(
        UUID id,
        String name,
        String email,
        UserRole role
) {
}
