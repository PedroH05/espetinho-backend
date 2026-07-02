package com.espetinho.api.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddCartItemRequest(
        @NotNull(message = "Produto e obrigatorio")
        UUID productId,

        @NotNull(message = "Quantidade e obrigatoria")
        @Min(value = 1, message = "Quantidade deve ser maior que zero")
        Integer quantity
) {
}
