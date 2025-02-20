package com.fondant.product.domain.repository;

import com.fondant.product.domain.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProductRepository extends JpaRepository<ProductEntity,Long>,ProductRepositoryCustom {
}
