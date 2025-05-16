package com.fondant.cart.application.dto;

import com.fondant.cart.domain.entity.CartItemOptionEntity;
import lombok.Builder;

@Builder
public record CartItemOption(
        Long optionId,
        String optionName,
        int optionPrice,
        int optionQuantity
) {
    public static CartItemOption from(CartItemOptionEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("CartItemOptionEntity가 null");
        }

        if (entity.getOption() == null) {
            throw new IllegalArgumentException("CartItemOptionEntity에 연결된 OptionEntity가 null");
        }
        return CartItemOption.builder()
                .optionId(entity.getOption().getId())
                .optionName(entity.getOption().getName())
                .optionPrice(entity.getOption().getPrice())
                .optionQuantity(entity.getQuantity())
                .build();
    }
}