package com.espetinho.api.order.dto;

import com.espetinho.api.order.enums.OrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
        @NotNull(message = "Tipo do pedido e obrigatorio")
        OrderType type,

        @Valid
        CustomerInfoRequest customer,

        @Valid
        DeliveryAddressRequest deliveryAddress,

        @Size(max = 500, message = "Observacao deve ter no maximo 500 caracteres")
        String notes
) {
}
