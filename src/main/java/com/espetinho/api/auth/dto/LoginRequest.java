package com.espetinho.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para login tradicional com e-mail e senha")
public record LoginRequest(
        @Schema(example = "usuario@email.com")
        @NotBlank(message = "E-mail e obrigatorio")
        @Email(message = "E-mail invalido")
        String email,

        @Schema(example = "123456")
        @NotBlank(message = "Senha e obrigatoria")
        String password
) {
}
