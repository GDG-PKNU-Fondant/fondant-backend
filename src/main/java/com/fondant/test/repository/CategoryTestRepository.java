package com.fondant.test.repository;

import com.fondant.product.category.domain.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryTestRepository extends JpaRepository<CategoryEntity,Long> {
}
