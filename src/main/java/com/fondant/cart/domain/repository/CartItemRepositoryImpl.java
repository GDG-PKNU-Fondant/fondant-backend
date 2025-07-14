package com.fondant.cart.domain.repository;

import com.fondant.cart.domain.entity.CartItemEntity;
import com.fondant.cart.domain.entity.QCartEntity;
import com.fondant.cart.domain.entity.QCartItemEntity;
import com.fondant.cart.domain.entity.QCartMarketEntity;
import com.fondant.user.domain.entity.QUserEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class CartItemRepositoryImpl implements CartItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<CartItemEntity> findByIdAndUserId(Long cartItemId, Long userId) {
        QCartItemEntity cartItem = QCartItemEntity.cartItemEntity;
        QCartMarketEntity cartMarket = QCartMarketEntity.cartMarketEntity;
        QCartEntity cart = QCartEntity.cartEntity;
        QUserEntity user = QUserEntity.userEntity;

        CartItemEntity result = queryFactory
                .select(cartItem)
                .from(cartItem)
                .join(cartItem.cartMarket, cartMarket).fetchJoin()
                .join(cartMarket.cart, cart).fetchJoin()
                .join(cart.user, user)
                .where(cartItem.id.eq(cartItemId), user.id.eq(userId))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
