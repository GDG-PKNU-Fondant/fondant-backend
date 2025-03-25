package com.fondant.order.domain.entity;

public enum DeliveryStatus {
    PENDING,
    PROCESSING,
    SHIPPED_OUT,
    IN_DELIVERY,
    DELIVERED,
    FAILED,
    REFUND_REQUESTED,
    REFUNDED,
    CANCELED
}