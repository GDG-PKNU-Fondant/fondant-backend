package com.fondant.test.repository;

import com.fondant.product.domain.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryTestRepository extends JpaRepository<CategoryEntity,Long> {
}
