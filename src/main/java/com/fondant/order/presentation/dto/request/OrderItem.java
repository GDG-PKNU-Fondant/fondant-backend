package com.fondant.order.presentation.dto.request;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OrderItem(
        Long marketId,
        Long optionId,
        Long productId,
        int quantity,
        BigDecimal price
) {
}
