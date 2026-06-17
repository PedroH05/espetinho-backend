package com.espetinho.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para definir nova senha usando codigo de recuperacao")
public record ResetPasswordRequest(
        @Schema(example = "usuario@email.com")
        @NotBlank(message = "E-mail e obrigatorio")
        @Email(message = "E-mail invalido")
        String email,

        @Schema(example = "483921")
        @NotBlank(message = "Codigo e obrigatorio")
        @Pattern(regexp = "\\d{6}", message = "Codigo deve conter 6 digitos")
        String code,

        @Schema(example = "novaSenha123")
        @NotBlank(message = "Nova senha e obrigatoria")
        @Size(min = 6, max = 72, message = "Nova senha deve ter entre 6 e 72 caracteres")
        String newPassword
) {
}
