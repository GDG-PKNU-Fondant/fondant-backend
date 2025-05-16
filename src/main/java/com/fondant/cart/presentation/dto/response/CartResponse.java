package com.fondant.cart.presentation.dto.response;

import com.fondant.cart.application.dto.CartInfo;
import com.fondant.cart.domain.entity.CartItemEntity;
import com.fondant.global.dto.PageInfo;
import lombok.Builder;

import java.util.List;

@Builder
public record CartResponse(
        PageInfo pageInfo,
        List<CartInfo> cartItems
) {
    public static CartResponse of(List<CartItemEntity> cartItems, PageInfo pageInfo) {
        return CartResponse.builder()
                .pageInfo(pageInfo)
                .cartItems(cartItems.stream().map(CartInfo::from).toList())
                .build();
    }
}