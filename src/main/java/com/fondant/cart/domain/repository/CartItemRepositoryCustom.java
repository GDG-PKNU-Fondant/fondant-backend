package com.fondant.cart.domain.repository;

import com.fondant.cart.domain.entity.CartItemEntity;

import java.util.Optional;

public interface CartItemRepositoryCustom {
    Optional<CartItemEntity> findByIdAndUserId(Long cartItemId, Long userId);
}
