package com.fondant.product.application.dto;

import com.fondant.market.application.dto.MarketInfo;
import lombok.Builder;

public record ProductInfo(
        Long id,
        String name,
        Double price,
        String thumbnailUrl,
        double score,
        Double discountRate,
        Double discountPrice,
        String marketName,
        Long marketId
) {
    @Builder
    public ProductInfo {}
}
