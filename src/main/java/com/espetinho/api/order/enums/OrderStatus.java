package com.espetinho.api.order.enums;

public enum OrderStatus {
    AWAITING_PAYMENT,
    PAYMENT_APPROVED,
    PREPARING,
    READY_FOR_PICKUP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    FINISHED,
    CANCELLED
}
