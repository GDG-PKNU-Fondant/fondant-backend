package com.fondant.product.presentation.dto.response;

import com.fondant.global.dto.PageInfo;
import com.fondant.product.application.dto.ProductInfo;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
public record ProductsResponse(
        PageInfo pageInfo,
        List<ProductInfo> products
) {
    public static ProductsResponse of(List<ProductInfo> products,PageInfo pageInfo) {
        return ProductsResponse.builder()
                .pageInfo(pageInfo)
                .products(products)
                .build();
    }
}
