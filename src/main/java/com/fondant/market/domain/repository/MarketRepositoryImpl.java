package com.fondant.market.domain.repository;

import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.market.domain.entity.QMarketCategoryEntity;
import com.fondant.market.domain.entity.QMarketEntity;
import com.fondant.product.domain.entity.QCategoryEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class MarketRepositoryImpl implements MarketRepositoryCustom {

    private final JPAQueryFactory queryFactory;

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
                .offset(pageable.getOffset()) // 페이지네이션 적용
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
}