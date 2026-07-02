package com.espetinho.api.order.dto;

import com.espetinho.api.order.enums.OrderStatus;
import com.espetinho.api.order.enums.OrderType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID userId,
        UUID guestId,
        CustomerInfoResponse customer,
        OrderType type,
        OrderStatus status,
        DeliveryAddressResponse deliveryAddress,
        List<OrderItemResponse> items,
        BigDecimal totalAmount,
        Instant createdAt
) {
}
