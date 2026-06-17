package com.espetinho.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para atualizacao do perfil do usuario autenticado")
public record UpdateUserProfileRequest(
        @Schema(example = "Pedro Henrique")
        @NotBlank(message = "Nome e obrigatorio")
        @Size(max = 120, message = "Nome deve ter no maximo 120 caracteres")
        String name
) {
}
