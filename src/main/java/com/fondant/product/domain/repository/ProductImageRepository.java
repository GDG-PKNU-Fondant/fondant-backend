package com.fondant.product.domain.repository;

import com.fondant.product.domain.entity.ImageType;
import com.fondant.product.domain.entity.ProductImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImageEntity,Long> {
    List<ProductImageEntity> findByProductIdAndImageType(Long productId, ImageType type);
}
