package com.fondant.order.presentation.dto.request;

import com.fondant.order.presentation.dto.CheckoutItem;

import java.util.List;

public record OrderPrepareRequest(
        List<CheckoutItem> items
) {
}