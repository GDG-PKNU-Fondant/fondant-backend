package com.fondant.cart.application.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record CartInfo(
        Long productId,
        int quantity,
        List<OptionInfo> options
) {
    @Builder
    public record OptionInfo(
            Long optionId,
            int quantity
    ) {}
}
