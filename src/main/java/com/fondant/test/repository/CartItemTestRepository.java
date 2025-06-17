package com.fondant.test.repository;

import com.fondant.cart.domain.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemTestRepository extends JpaRepository<CartItemEntity, Long> {
}
