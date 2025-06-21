package com.fondant.order.presentation.dto.request;

import java.util.List;

public record OrderPrepareRequest(
        List<OrderItem> items
) {
    public record OrderItem(
            Long productId,
            Long optionId,
            int quantity
    ) {}

    public Iterable<? extends OrderItem> getItems() {
        return items;
    }
}