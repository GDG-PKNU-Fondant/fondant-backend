package com.fondant.cart.domain.repository;

import com.fondant.cart.domain.entity.*;
import com.fondant.user.domain.entity.QUserEntity;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

@RequiredArgsConstructor
public class CartRepositoryImpl implements CartRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<CartItemEntity> findCartItemsByUser(Long userId, Pageable pageable) {
        QCartEntity cart = QCartEntity.cartEntity;
        QCartMarketEntity cartMarket = QCartMarketEntity.cartMarketEntity;
        QCartItemEntity cartItem = QCartItemEntity.cartItemEntity;
        QCartItemOptionEntity cartItemOption = QCartItemOptionEntity.cartItemOptionEntity;
        QUserEntity user = QUserEntity.userEntity;

        List<CartItemEntity> content = queryFactory
                .selectFrom(cartItem).distinct()
                .join(cartItem.cartMarket, cartMarket).fetchJoin()
                .join(cartMarket.cart, cart).fetchJoin()
                .join(cart.user, user).fetchJoin()
                .join(cartItem.product).fetchJoin()
                .leftJoin(cartItem.cartItemOptions, cartItemOption).fetchJoin()
                .leftJoin(cartItemOption.option).fetchJoin()
                .where(user.id.eq(userId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(cartItem.count())
                .from(cartItem)
                .join(cartItem.cartMarket, cartMarket)
                .join(cartMarket.cart, cart)
                .join(cart.user, user)
                .where(user.id.eq(userId));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
