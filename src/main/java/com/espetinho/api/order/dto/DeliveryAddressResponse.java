package com.espetinho.api.order.dto;

public record DeliveryAddressResponse(
        String street,
        String number,
        String complement,
        String neighborhood
) {
}
