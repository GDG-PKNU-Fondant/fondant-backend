package com.fondant.cart.domain.repository;

import com.fondant.cart.domain.entity.CartEntity;
import com.fondant.cart.domain.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<CartEntity, Long>, CartRepositoryCustom {
   Optional<CartEntity> findByUserId(Long userId);
   Optional<CartItemEntity> findCartItemByIdAndUserId(Long cartItemId, Long userId);
}

