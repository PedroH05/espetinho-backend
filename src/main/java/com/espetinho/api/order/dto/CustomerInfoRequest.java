package com.espetinho.api.order.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerInfoRequest(
        @NotBlank(message = "Nome do cliente e obrigatorio")
        @Size(max = 120, message = "Nome do cliente deve ter no maximo 120 caracteres")
        String name,

        @NotBlank(message = "Telefone do cliente e obrigatorio")
        @Size(max = 30, message = "Telefone deve ter no maximo 30 caracteres")
        String phone,

        @Email(message = "E-mail invalido")
        @Size(max = 180, message = "E-mail deve ter no maximo 180 caracteres")
        String email
) {
}
