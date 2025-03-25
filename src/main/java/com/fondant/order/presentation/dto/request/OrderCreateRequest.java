package com.fondant.order.presentation.dto.request;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record OrderCreateRequest(
        List<OrderItem> items,
        Long addressId,
        BigDecimal price
) {
}
