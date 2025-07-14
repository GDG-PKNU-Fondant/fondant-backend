package com.fondant.cart.domain.repository;

import com.fondant.cart.domain.entity.*;
import com.fondant.market.domain.entity.QMarketEntity;
import com.fondant.product.domain.entity.QOptionEntity;
import com.fondant.product.domain.entity.QProductEntity;
import com.fondant.user.domain.entity.QUserEntity;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CartRepositoryImpl implements CartRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<CartItemEntity> findCartItemsByUser(Long userId, Pageable pageable) {
        QCartEntity cart = QCartEntity.cartEntity;
        QCartMarketEntity cartMarket = QCartMarketEntity.cartMarketEntity;
        QCartItemEntity cartItem = QCartItemEntity.cartItemEntity;
        QCartItemOptionEntity cartItemOption = QCartItemOptionEntity.cartItemOptionEntity;
        QProductEntity product = QProductEntity.productEntity;
        QOptionEntity option = QOptionEntity.optionEntity;
        QMarketEntity market = QMarketEntity.marketEntity;
        QUserEntity user = QUserEntity.userEntity;

        JPAQuery<CartItemEntity> query = queryFactory
                .selectDistinct(cartItem)
                .from(cartItem)
                .join(cartItem.cartMarket, cartMarket).fetchJoin()
                .join(cartMarket.cart, cart).fetchJoin()
                .join(cart.user, user).fetchJoin()
                .join(cartMarket.market, market).fetchJoin()
                .join(cartItem.product, product).fetchJoin()
                .leftJoin(cartItem.cartItemOptions, cartItemOption).fetchJoin()
                .leftJoin(cartItemOption.option, option).fetchJoin()
                .where(user.id.eq(userId));

        if (!pageable.isUnpaged()) {
            query.offset(pageable.getOffset()).limit(pageable.getPageSize());
        }

        List<CartItemEntity> content = query.fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(cartItem.count())
                .from(cartItem)
                .join(cartItem.cartMarket, cartMarket)
                .join(cartMarket.cart, cart)
                .join(cart.user, user)
                .where(user.id.eq(userId));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Optional<CartEntity> findByUserId(Long userId) {
        QCartEntity cart = QCartEntity.cartEntity;
        QUserEntity user = QUserEntity.userEntity;

        CartEntity cartEntity = queryFactory
                .selectFrom(cart)
                .join(cart.user, user).fetchJoin()
                .leftJoin(cart.cartMarkets).fetchJoin()
                .where(user.id.eq(userId))
                .fetchOne();

        return Optional.ofNullable(cartEntity);
    }
}