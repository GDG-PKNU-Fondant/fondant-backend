package com.fondant.payment.presentation.dto;

public record PaymentRequest(
        String paymentId,
        Long orderId
) {
}
