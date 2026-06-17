package com.espetinho.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para solicitar recuperacao de senha")
public record ForgotPasswordRequest(
        @Schema(example = "usuario@email.com")
        @NotBlank(message = "E-mail e obrigatorio")
        @Email(message = "E-mail invalido")
        String email
) {
}
