package com.fondant.order.presentation.dto.request;

import lombok.Builder;

import java.util.List;

@Builder
public record OrderCreateRequest(
        List<OrderItem> items,

        Long addressId,
        double totalPrice
) {
}
