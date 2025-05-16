package com.fondant.cart.application.dto;

import com.fondant.cart.domain.entity.CartItemEntity;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record CartInfo(
        Long cartItemId,
        Long marketId,
        String marketName,
        Long productId,
        String productName,
        int quantity,
        LocalDate arrivalDate,
        List<CartItemOption> options
) {
    public static CartInfo from(CartItemEntity entity) {
        return CartInfo.builder()
                .cartItemId(entity.getId())
                .marketId(entity.getCartMarket() != null && entity.getCartMarket().getMarket() != null
                        ? entity.getCartMarket().getMarket().getId()
                        : null)
                .marketName(entity.getCartMarket() != null && entity.getCartMarket().getMarket() != null
                        ? entity.getCartMarket().getMarket().getName()
                        : null)
                .productId(entity.getProduct() != null ? entity.getProduct().getId() : null)
                .productName(entity.getProduct() != null ? entity.getProduct().getName() : null)
                .quantity(entity.getQuantity())
                .arrivalDate(entity.getArrivalDate())
                .options(entity.getCartItemOptions() == null ?
                        List.of() :
                        entity.getCartItemOptions().stream()
                                .filter(opt -> opt.getOption() != null)
                                .map(CartItemOption::from)
                                .toList())
                .build();
    }
}