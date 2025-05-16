package com.fondant.cart.application.dto;

import com.fondant.cart.domain.entity.CartItemOptionEntity;
import lombok.Builder;

@Builder
public record CartOptionInfo(
        Long optionId,
        String optionName,
        int optionPrice,
        int quantity
) {
    public static CartOptionInfo from(CartItemOptionEntity option) {
        return CartOptionInfo.builder()
                .optionId(option.getOption().getId())
                .optionName(option.getOption().getName())
                .optionPrice(option.getOption().getPrice())
                .quantity(option.getQuantity())
                .build();
    }
}