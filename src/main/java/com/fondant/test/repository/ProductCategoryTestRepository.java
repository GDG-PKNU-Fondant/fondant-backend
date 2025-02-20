package com.fondant.test.repository;

import com.fondant.product.domain.entity.ProductCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryTestRepository extends JpaRepository<ProductCategoryEntity,Long> {
}
