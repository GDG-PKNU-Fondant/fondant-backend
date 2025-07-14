package com.fondant.cart.domain.repository;
import com.fondant.cart.domain.entity.CartItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CartRepositoryCustom {
    Page<CartItemEntity> findCartItemsByUser(Long userId, Pageable pageable);
}