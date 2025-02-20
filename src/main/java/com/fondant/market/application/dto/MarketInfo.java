package com.fondant.market.application.dto;

import lombok.Builder;
@Builder
public record MarketInfo (
        Long id,
        String name,
        Long totalSales,
        Long totalReviews,
        String description,
        String thumbnail
) {
}