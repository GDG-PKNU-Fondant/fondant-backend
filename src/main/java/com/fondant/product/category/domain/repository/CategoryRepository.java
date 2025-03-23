package com.fondant.product.category.domain.repository;

import com.fondant.product.category.domain.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity,Long> {
}
