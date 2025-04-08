package com.fondant.product.category.application.dto;

import lombok.Builder;

@Builder
public record CategoryInfo(
        Long id,
        String name
) {
}
