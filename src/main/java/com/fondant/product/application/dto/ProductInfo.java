package com.fondant.product.application.dto;

import lombok.Builder;

@Builder
public record ProductInfo(
        Long id,
        String name,
        Double price,
        String thumbnailUrl,
        double score,
        Double discountRate,
        Double discountPrice
) {
}
