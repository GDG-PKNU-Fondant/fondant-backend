package com.fondant.cart.presentation.dto.response;

import java.util.List;

public record CartItemResponse(
        Long productId,
        String productName,
        int quantity,
        List<OptionResponse> options
) {
    public record OptionResponse(
            Long optionId,
            String optionName,
            int quantity
    ) {}
}
