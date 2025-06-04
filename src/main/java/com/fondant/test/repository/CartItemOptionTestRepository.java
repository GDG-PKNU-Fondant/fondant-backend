package com.fondant.test.repository;

import com.fondant.cart.domain.entity.CartItemOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemOptionTestRepository extends JpaRepository<CartItemOptionEntity, Long> {
}
