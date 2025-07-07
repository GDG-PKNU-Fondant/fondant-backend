package com.fondant.market.domain.repository;

import com.fondant.global.exception.ApiException;
import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.market.domain.entity.QMarketCategoryEntity;
import com.fondant.market.domain.entity.QMarketEntity;
import com.fondant.market.domain.entity.QMarketLikeEntity;
import com.fondant.market.exception.MarketError;
import com.fondant.product.category.domain.QCategoryEntity;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class MarketRepositoryImpl implements MarketRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @PersistenceContext
    private EntityManager entityManager;

    public MarketRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    private NumberExpression<Double> calculatePopularityScore(QMarketEntity market) {
        return market.totalSales
                .coalesce(0L).castToNum(Double.class)
                .add(market.totalReviews.coalesce(0L).castToNum(Double.class).multiply(2.0))
                .add(Expressions.numberTemplate(Double.class, "GREATEST(0, {0})", 100));
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

    /*
    // 부모/자식 카테고리 포함 통합 조회
    public Page<MarketEntity> findMarketsByCategoryWithSubCategories(Long categoryId, Pageable pageable) {
        QMarketEntity market = QMarketEntity.marketEntity;
        QMarketCategoryEntity marketCategory = QMarketCategoryEntity.marketCategoryEntity;
        QCategoryEntity category = QCategoryEntity.categoryEntity;

        // 모든 자식 카테고리 id를 포함한 id 리스트 조회
        List<Long> categoryIds = queryFactory
                .select(category.id)
                .from(category)
                .where(
                    category.id.eq(categoryId)
                        .or(category.parent.id.eq(categoryId))
                )
                .fetch();

        // 조회 쿼리 (IN 조건으로 부모/자식 모두 포함)
        List<MarketEntity> content = queryFactory
                .selectDistinct(market)
                .from(marketCategory)
                .join(marketCategory.market, market)
                .join(marketCategory.category, category)
                .where(category.id.in(categoryIds))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(market.count())
                .from(marketCategory)
                .join(marketCategory.market, market)
                .join(marketCategory.category, category)
                .where(category.id.in(categoryIds))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
*/

    @Override
    public Page<MarketEntity> findTop10MarketsByPopularity(Pageable pageable) {
        QMarketEntity market = QMarketEntity.marketEntity;

        NumberExpression<Double> popularityScore = calculatePopularityScore(market);

        List<MarketEntity> markets = queryFactory
                .selectFrom(market)
                .where(market.createAt.isNotNull())
                .orderBy(popularityScore.desc())
                .limit(10)
                .fetch();

        if (markets.isEmpty()) {
            throw new ApiException(MarketError.NO_MARKETS_FOUND);
        }

        return new PageImpl<>(markets, pageable, markets.size());
    }

    @Override
    public Page<MarketEntity> findRandomTop5MarketsByCategory(Long categoryId, Pageable pageable) {
        QMarketEntity market = QMarketEntity.marketEntity;
        QMarketCategoryEntity marketCategory = QMarketCategoryEntity.marketCategoryEntity;
        QCategoryEntity category = QCategoryEntity.categoryEntity;
        NumberExpression<Double> popularityScore = calculatePopularityScore(market);

        JPQLQuery<Long> subQuery = JPAExpressions
                .select(market.id)
                .from(marketCategory)
                .join(marketCategory.market, market)
                .join(marketCategory.category, category)
                .where(
                        category.id.eq(categoryId)
                                .and(market.createAt.isNotNull())
                )
                .orderBy(popularityScore.desc())
                .limit(30);

        List<MarketEntity> markets = queryFactory
                .selectFrom(market)
                .where(market.id.in(subQuery))
                .orderBy(Expressions.numberTemplate(Double.class, "random()").asc())
                .limit(5)
                .fetch();

        return new PageImpl<>(markets, pageable, markets.size());
    }

    /*
    // 부모/자식 카테고리 포함 통합 랜덤 Top5 조회
    public Page<MarketEntity> findRandomTop5MarketsByCategoryWithSubCategories(Long categoryId, Pageable pageable) {
        QMarketEntity market = QMarketEntity.marketEntity;
        QMarketCategoryEntity marketCategory = QMarketCategoryEntity.marketCategoryEntity;
        QCategoryEntity category = QCategoryEntity.categoryEntity;

        NumberExpression<Double> popularityScore = calculatePopularityScore(market);

        // 모든 자식 카테고리 id를 포함한 id 리스트 조회
        List<Long> categoryIds = queryFactory
                .select(category.id)
                .from(category)
                .where(
                    category.id.eq(categoryId)
                        .or(category.parent.id.eq(categoryId))
                )
                .fetch();

        // 인기순으로 30개까지 조회
        JPQLQuery<Long> subQuery = JPAExpressions
                .select(market.id)
                .from(marketCategory)
                .join(marketCategory.market, market)
                .join(marketCategory.category, category)
                .where(
                    category.id.in(categoryIds)
                        .and(market.createAt.isNotNull())
                )
                .orderBy(popularityScore.desc())
                .limit(30);

        // 30개 중 랜덤 5개 추출
        List<MarketEntity> markets = queryFactory
                .selectFrom(market)
                .where(market.id.in(subQuery))
                .orderBy(Expressions.numberTemplate(Double.class, "random()").asc())
                .limit(5)
                .fetch();

        return new PageImpl<>(markets, pageable, markets.size());
    }
*/

    @Override
    public Page<MarketEntity> findTop30MarketsByCategory(Long categoryId, Pageable pageable) {
        QMarketEntity market = QMarketEntity.marketEntity;
        QMarketCategoryEntity marketCategory = QMarketCategoryEntity.marketCategoryEntity;
        QCategoryEntity category = QCategoryEntity.categoryEntity;

        NumberExpression<Double> popularityScore = calculatePopularityScore(market);

        List<MarketEntity> top30Markets = queryFactory
                .select(market)
                .from(marketCategory)
                .join(marketCategory.market, market)
                .join(marketCategory.category, category)
                .where(
                        category.id.eq(categoryId)
                                .and(market.createAt.isNotNull())
                )
                .groupBy(
                        market.id,
                        market.createAt,
                        market.description,
                        market.name,
                        market.thumbnail,
                        market.totalReviews,
                        market.totalSales
                )
                .orderBy(popularityScore.desc())
                .limit(30)
                .fetch();

        if (top30Markets.isEmpty()) {
            throw new ApiException(MarketError.NO_MARKETS_FOUND);
        }

        long rawTotal = queryFactory
                .select(market.countDistinct())
                .from(marketCategory)
                .join(marketCategory.market, market)
                .join(marketCategory.category, category)
                .where(
                        category.id.eq(categoryId)
                                .and(market.createAt.isNotNull())
                )
                .fetchOne();

        long total = Math.min(rawTotal, 30);

        int totalInt = (int) total;
        int offset = (int) pageable.getOffset();
        int pageSize = pageable.getPageSize();
        int end = Math.min(offset + pageSize, totalInt);
        List<MarketEntity> pageContent = top30Markets.subList(offset, end);

        return new PageImpl<>(pageContent, pageable, total);
    }

    /*
    // 부모/자식 카테고리 포함 통합 top30 조회
    public Page<MarketEntity> findTop30MarketsByCategoryWithSubCategories(Long categoryId, Pageable pageable) {
        QMarketEntity market = QMarketEntity.marketEntity;
        QMarketCategoryEntity marketCategory = QMarketCategoryEntity.marketCategoryEntity;
        QCategoryEntity category = QCategoryEntity.categoryEntity;

        NumberExpression<Double> popularityScore = calculatePopularityScore(market);

        // 모든 자식 카테고리 id를 포함한 id 리스트 조회
        List<Long> categoryIds = queryFactory
                .select(category.id)
                .from(category)
                .where(
                    category.id.eq(categoryId)
                        .or(category.parent.id.eq(categoryId))
                )
                .fetch();

        // 조회 쿼리 (IN 조건으로 부모/자식 모두 포함)
        List<MarketEntity> top30Markets = queryFactory
                .select(market)
                .from(marketCategory)
                .join(marketCategory.market, market)
                .join(marketCategory.category, category)
                .where(
                    category.id.in(categoryIds)
                        .and(market.createAt.isNotNull())
                )
                .groupBy(
                    market.id,
                    market.createAt,
                    market.description,
                    market.name,
                    market.thumbnail,
                    market.totalReviews,
                    market.totalSales
                )
                .orderBy(popularityScore.desc())
                .limit(30)
                .fetch();

        if (top30Markets.isEmpty()) {
            throw new ApiException(MarketError.NO_MARKETS_FOUND);
        }

        long total = Math.min(top30Markets.size(), 30);

        int offset = (int) pageable.getOffset();
        int pageSize = pageable.getPageSize();
        int end = Math.min(offset + pageSize, top30Markets.size());
        List<MarketEntity> pageContent = top30Markets.subList(offset, end);

        return new PageImpl<>(pageContent, pageable, total);
    }
*/

    @Override
    public boolean isMarketInTop10ByCategory(Long marketId) {
        QMarketEntity market = QMarketEntity.marketEntity;
        QMarketCategoryEntity marketCategory = QMarketCategoryEntity.marketCategoryEntity;
        QCategoryEntity category = QCategoryEntity.categoryEntity;

        Long categoryId = queryFactory
                .select(marketCategory.category.id)
                .from(marketCategory)
                .where(marketCategory.market.id.eq(marketId))
                .fetchFirst();

        if (categoryId == null) return false;

        NumberExpression<Double> popularityScore = market.totalSales
                .coalesce(0L).castToNum(Double.class)
                .add(market.totalReviews.coalesce(0L).castToNum(Double.class).multiply(2.0))
                .add(Expressions.numberTemplate(Double.class, "GREATEST(0, {0})", 100));

        List<Long> top10Ids = queryFactory
                .select(market.id)
                .from(marketCategory)
                .join(marketCategory.market, market)
                .join(marketCategory.category, category)
                .where(category.id.eq(categoryId))
                .orderBy(popularityScore.desc())
                .limit(10)
                .fetch();

        return top10Ids.contains(marketId);
    }

    @Override
    public boolean isMarketLikedByUser(Long marketId, Long userId) {
        QMarketLikeEntity marketLike = QMarketLikeEntity.marketLikeEntity;

        Integer fetchOne = queryFactory
                .selectOne()
                .from(marketLike)
                .where(marketLike.market.id.eq(marketId)
                        .and(marketLike.user.id.eq(userId)))
                .fetchFirst();

        return fetchOne != null;
    }

    @Override
    public long countLikesByMarket(Long marketId) {
        QMarketLikeEntity marketLike = QMarketLikeEntity.marketLikeEntity;

        return queryFactory
                .select(marketLike.count())
                .from(marketLike)
                .where(marketLike.market.id.eq(marketId))
                .fetchOne();
    }

    @Override
    public List<Long> findSubCategoryIdsByMarketId(Long marketId) {
        QMarketCategoryEntity marketCategory = QMarketCategoryEntity.marketCategoryEntity;

        return queryFactory
                .select(marketCategory.category.id)
                .from(marketCategory)
                .where(marketCategory.market.id.eq(marketId))
                .distinct()
                .fetch();
    }
}