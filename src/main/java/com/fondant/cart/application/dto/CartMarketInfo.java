package com.fondant.cart.application.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record CartMarketInfo (
        Long marketId,
        String marketName,
        List<CartProductInfo> products
) {}
