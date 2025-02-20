package com.fondant.product.domain.repository;

import com.fondant.product.domain.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {
    Page<ProductEntity> findProductsByMarketAndCategory(Long marketId, Long categoryId, Pageable pageable);
}
