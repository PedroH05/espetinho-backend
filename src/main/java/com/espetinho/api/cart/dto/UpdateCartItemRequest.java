package com.espetinho.api.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemRequest(
        @NotNull(message = "Quantidade e obrigatoria")
        @Min(value = 1, message = "Quantidade deve ser maior que zero")
        Integer quantity
) {
}
