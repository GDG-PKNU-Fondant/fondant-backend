package com.fondant.test.repository;

import com.fondant.cart.domain.entity.CartMarketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartMarketTestRepository extends JpaRepository<CartMarketEntity, Long> {
}