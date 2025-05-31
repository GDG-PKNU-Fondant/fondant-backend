package com.fondant.product.domain.repository;

import com.fondant.product.domain.entity.ProductCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryRepository extends JpaRepository<ProductCategoryEntity,Long> {
}
