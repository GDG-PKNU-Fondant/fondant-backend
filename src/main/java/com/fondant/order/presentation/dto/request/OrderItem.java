package com.fondant.order.presentation.dto.request;

import lombok.Builder;

@Builder
public record OrderItem(
        Long marketId,
        Long optionId,
        Long productId,
        int quantity,
        double discountRate,
        double price,
        double optionPrice,
        Long deliveryFee
) {
}