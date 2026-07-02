package com.espetinho.api.cart.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID id,
        UUID userId,
        UUID guestId,
        List<CartItemResponse> items,
        Integer totalItems,
        BigDecimal totalAmount
) {
}
