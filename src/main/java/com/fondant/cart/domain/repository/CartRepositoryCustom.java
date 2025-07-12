package com.fondant.cart.domain.repository;
import com.fondant.cart.domain.entity.CartEntity;
import com.fondant.cart.domain.entity.CartItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CartRepositoryCustom {
    Page<CartItemEntity> findCartItemsByUser(Long userId, Pageable pageable);

    Optional<CartEntity> findByUserId(Long userId);

    Optional<CartItemEntity> findCartItemByIdAndUserId(Long cartItemId, Long userId);
}