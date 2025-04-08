package com.fondant.product.category.application.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record MainCategoryInfo(
        Long id,
        String name,
        List<CategoryInfo> subCategories
) {
}
