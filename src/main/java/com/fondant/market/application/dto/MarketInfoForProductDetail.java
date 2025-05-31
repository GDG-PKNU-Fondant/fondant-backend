package com.fondant.market.application.dto;

import lombok.Builder;

@Builder
public record MarketInfoForProductDetail(
        Long id,
        String name,
        Long totalSales,
        Long totalReviews,
        String description,
        String thumbnail,
        Double freeDeliveryLimit
) {
}
