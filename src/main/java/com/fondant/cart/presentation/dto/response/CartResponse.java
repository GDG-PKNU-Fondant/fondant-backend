package com.fondant.cart.presentation.dto.response;

import com.fondant.cart.application.dto.CartMarketInfo;
import com.fondant.cart.domain.entity.CartItemEntity;
import com.fondant.global.dto.PageInfo;
import lombok.Builder;

import java.util.List;

@Builder
public record CartResponse(
        PageInfo pageInfo,
        List<CartMarketInfo> markets
) {
    public static CartResponse of(List<CartMarketInfo> marketInfos, PageInfo pageInfo) {
        return CartResponse.builder()
                .markets(marketInfos)
                .pageInfo(pageInfo)
                .build();
    }
}