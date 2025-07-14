package com.fondant.cart.domain.repository;

import com.fondant.cart.domain.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long>, CartItemRepositoryCustom {
    Optional<CartItemEntity> findByIdAndUserId(Long cartItemId, Long userId);
}