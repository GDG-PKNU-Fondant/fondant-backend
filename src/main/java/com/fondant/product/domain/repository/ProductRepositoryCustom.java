package com.fondant.product.domain.repository;

import com.fondant.product.application.dto.FilterInfo;
import com.fondant.product.application.dto.SortType;
import com.fondant.product.domain.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductRepositoryCustom {
    Page<ProductEntity> findProductsByMarketAndCategory(Long marketId, Long categoryId, Pageable pageable);
    Long countProductsByFilter(FilterInfo filterInfo);
    Page<ProductEntity> findFilteredProducts(FilterInfo filterInfo, Pageable pageable, Optional<SortType> sortType);
}
