package com.espetinho.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta do login tradicional")
public record LoginResponse(
        String token,
        AuthenticatedUserResponse user
) {
}
