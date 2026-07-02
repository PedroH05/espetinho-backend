package com.espetinho.api.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeliveryAddressRequest(
        @NotBlank(message = "Rua e obrigatoria")
        @Size(max = 180, message = "Rua deve ter no maximo 180 caracteres")
        String street,

        @NotBlank(message = "Numero e obrigatorio")
        @Size(max = 30, message = "Numero deve ter no maximo 30 caracteres")
        String number,

        @Size(max = 120, message = "Complemento deve ter no maximo 120 caracteres")
        String complement,

        @NotBlank(message = "Bairro e obrigatorio")
        @Size(max = 120, message = "Bairro deve ter no maximo 120 caracteres")
        String neighborhood
) {
}
