package com.fondant.product.category.application.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record CategoryDetailsInfo(
        Long id,
        String name,
        String iconUrl,
        List<CategoryInfo> subCategories
) {
}
