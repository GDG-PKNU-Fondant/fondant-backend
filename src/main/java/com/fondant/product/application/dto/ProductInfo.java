package com.fondant.product.application.dto;

import lombok.Builder;

@Builder
public record ProductInfo(
        Long id,
        String name,
        int price,
        String thumbnailUrl,
        double score,
        double discountRate,
        int discountPrice
) {
}
