package com.fondant.infra.portone.dto;

public record CancellationResponse(
        String status,
        String id,
        String pgCancellationId,
        Long totalAmount,
        Long taxFreeAmount,
        Long vatAmount,
        Long easyPayDiscountAmount,
        String reason,
        String cancelledAt,
        String requestedAt
) {
}
