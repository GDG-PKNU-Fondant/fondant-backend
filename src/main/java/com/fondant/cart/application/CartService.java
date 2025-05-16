package com.fondant.cart.application;

import com.fondant.cart.domain.entity.CartItemEntity;
import com.fondant.cart.domain.repository.CartRepository;
import com.fondant.global.config.PageConfig;
import com.fondant.global.dto.PageInfo;
import com.fondant.cart.presentation.dto.response.CartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final PageConfig pageConfig;

    @Transactional(readOnly = true)
    public CartResponse getCartItemsByUser(Long userId, Pageable pageable) {
        Pageable effectivePageable = (pageable == null) ? pageConfig.defaultPageable() : pageable;
        Page<CartItemEntity> page = cartRepository.findCartItemsByUser(userId, effectivePageable);

        return CartResponse.of(page.getContent(), PageInfo.of(page.getNumber(), page.getTotalPages()));
    }
}