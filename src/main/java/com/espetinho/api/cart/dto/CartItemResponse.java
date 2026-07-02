package com.espetinho.api.cart.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID id,
        UUID productId,
        String productName,
        String imageUrl,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal,
        Boolean available
) {
}
