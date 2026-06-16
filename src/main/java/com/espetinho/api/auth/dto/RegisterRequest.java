package com.espetinho.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro tradicional com e-mail e senha")
public record RegisterRequest(
        @Schema(example = "Pedro Henrique")
        @NotBlank(message = "Nome e obrigatorio")
        @Size(max = 120, message = "Nome deve ter no maximo 120 caracteres")
        String name,

        @Schema(example = "usuario@email.com")
        @NotBlank(message = "E-mail e obrigatorio")
        @Email(message = "E-mail invalido")
        @Size(max = 180, message = "E-mail deve ter no maximo 180 caracteres")
        String email,

        @Schema(example = "123456")
        @NotBlank(message = "Senha e obrigatoria")
        @Size(min = 6, max = 72, message = "Senha deve ter entre 6 e 72 caracteres")
        String password
) {
}
