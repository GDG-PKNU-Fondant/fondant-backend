package com.fondant.market.domain.repository;

import com.fondant.global.exception.ApiException;
import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.market.domain.entity.QMarketCategoryEntity;
import com.fondant.market.domain.entity.QMarketEntity;
import com.fondant.market.exception.MarketError;
import com.fondant.product.domain.entity.QCategoryEntity;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MarketRepositoryImpl implements MarketRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @PersistenceContext
    private EntityManager entityManager;

    public MarketRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<MarketEntity> findMarketsByCategory(Long categoryId, Pageable pageable) {
        QMarketEntity market = QMarketEntity.marketEntity;
        QMarketCategoryEntity marketCategory = QMarketCategoryEntity.marketCategoryEntity;
        QCategoryEntity category = QCategoryEntity.categoryEntity;

        List<MarketEntity> content = queryFactory
                .selectDistinct(market)
                .from(marketCategory)
                .join(marketCategory.market, market)
                .join(marketCategory.category, category)
                .where(category.id.eq(categoryId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(market.count())
                .from(marketCategory)
                .join(marketCategory.market, market)
                .join(marketCategory.category, category)
                .where(category.id.eq(categoryId))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<MarketEntity> findTop10MarketsByPopularity() {
        QMarketEntity market = QMarketEntity.marketEntity;

        NumberExpression<Double> popularityScore = market.totalSales
                .coalesce(0L).castToNum(Double.class)
                .add(market.totalReviews.coalesce(0L).castToNum(Double.class).multiply(2.0))
                .add(Expressions.numberTemplate(Double.class, "GREATEST(0, {0})", 100));

        List<MarketEntity> markets = queryFactory
                .selectFrom(market)
                .where(market.createAt.isNotNull())
                .orderBy(popularityScore.desc())
                .limit(10)
                .fetch();

        if (markets.isEmpty()) {
            throw new ApiException(MarketError.NO_MARKETS_FOUND);
        }

        return markets;
    }

    @Override
    public List<MarketEntity> findTop5MarketsByPopularity() {
        QMarketEntity market = QMarketEntity.marketEntity;

        NumberExpression<Double> popularityScore = market.totalSales
                .coalesce(0L).castToNum(Double.class)
                .add(market.totalReviews.coalesce(0L).castToNum(Double.class).multiply(2.0))
                .add(Expressions.numberTemplate(Double.class, "GREATEST(0, {0})", 100));

        List<MarketEntity> markets = queryFactory
                .selectFrom(market)
                .where(market.createAt.isNotNull())
                .orderBy(popularityScore.desc())
                .limit(30)
                .fetch();

        Collections.shuffle(markets);
        return markets.stream().limit(5).collect(Collectors.toList());
    }

    @Override
    public List<MarketEntity> findTop30MarketsByPopularity() {
        QMarketEntity market = QMarketEntity.marketEntity;

        NumberExpression<Double> popularityScore = market.totalSales
                .coalesce(0L).castToNum(Double.class)
                .add(market.totalReviews.coalesce(0L).castToNum(Double.class).multiply(2.0))
                .add(Expressions.numberTemplate(Double.class, "GREATEST(0, {0})", 100));

        List<MarketEntity> markets = queryFactory
                .selectFrom(market)
                .where(market.createAt.isNotNull())
                .orderBy(popularityScore.desc())
                .limit(30)
                .fetch();

        if (markets.isEmpty()) {
            throw new ApiException(MarketError.NO_MARKETS_FOUND);
        }

        return markets;
    }
}