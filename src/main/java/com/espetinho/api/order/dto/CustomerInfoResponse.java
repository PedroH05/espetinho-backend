package com.espetinho.api.order.dto;

public record CustomerInfoResponse(
        String name,
        String phone,
        String email
) {
}
