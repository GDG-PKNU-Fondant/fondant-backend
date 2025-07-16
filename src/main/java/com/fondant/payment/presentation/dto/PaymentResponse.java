package com.fondant.payment.presentation.dto;

import lombok.Builder;

@Builder
public record PaymentResponse(
        Integer amount,
        String status,
        String paymentMethod
) {
}