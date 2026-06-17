package com.espetinho.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Dados para validar codigo de recuperacao de senha")
public record VerifyResetCodeRequest(
        @Schema(example = "usuario@email.com")
        @NotBlank(message = "E-mail e obrigatorio")
        @Email(message = "E-mail invalido")
        String email,

        @Schema(example = "483921")
        @NotBlank(message = "Codigo e obrigatorio")
        @Pattern(regexp = "\\d{6}", message = "Codigo deve conter 6 digitos")
        String code
) {
}
