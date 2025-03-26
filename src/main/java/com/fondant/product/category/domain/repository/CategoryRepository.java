package com.fondant.product.category.domain.repository;

import com.fondant.product.category.domain.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<CategoryEntity,Long> {
    List<CategoryEntity> getByParentId(Long parentId);
}
