package com.fondant.test.repository;

import com.fondant.cart.domain.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartTestRepository extends JpaRepository<CartEntity, Long> {
}