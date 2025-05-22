package com.fondant.product.category.presentation.dto.response;

import java.util.List;

public record CategoriesResponse<D>(
        List<D> categories
) {
    public static <D> CategoriesResponse<D> of(List<D> categories) {
        return new CategoriesResponse<>(categories);
    }
}
