package com.fondant.product.domain.repository;

import com.fondant.product.application.dto.FilterInfo;
import com.fondant.product.application.dto.SortType;
import com.fondant.product.category.domain.QCategoryEntity;
import com.fondant.product.domain.entity.PackagingType;
import com.fondant.product.domain.entity.ProductEntity;
import com.fondant.product.domain.entity.QProductCategoryEntity;
import com.fondant.product.domain.entity.QProductEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.fondant.product.application.dto.SortType.*;

public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public ProductRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<ProductEntity> findProductsByMarketAndCategory(Long marketId, Long subCategoryId, Pageable pageable) {
        List<ProductEntity> content = findProductsByMarketAndSubCategory(marketId, subCategoryId, pageable);
        Long total = countProductsByMarketAndSubCategory(marketId, subCategoryId);

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private List<Long> findSubCategoryIdsByMainCategory(Long mainCategoryId) {
        QCategoryEntity category = QCategoryEntity.categoryEntity;

        return queryFactory
                .select(category.id)
                .from(category)
                .where(category.parent.id.eq(mainCategoryId))
                .fetch();
    }

    private List<ProductEntity> findProductsByMarketAndSubCategory(Long marketId, Long subCategoryId, Pageable pageable) {
        QProductEntity product = QProductEntity.productEntity;
        QProductCategoryEntity productCategory = QProductCategoryEntity.productCategoryEntity;
        QCategoryEntity category = QCategoryEntity.categoryEntity;

        return queryFactory
                .selectDistinct(product)
                .from(product)
                .join(productCategory).on(product.eq(productCategory.product))
                .join(productCategory.category, category)
                .where(
                        product.market.id.eq(marketId),
                        category.id.eq(subCategoryId)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private Long countProductsByMarketAndSubCategory(Long marketId, Long subCategoryId) {
        QProductEntity product = QProductEntity.productEntity;
        QProductCategoryEntity productCategory = QProductCategoryEntity.productCategoryEntity;
        QCategoryEntity category = QCategoryEntity.categoryEntity;

        return queryFactory
                .select(product.countDistinct())
                .from(product)
                .join(productCategory).on(product.eq(productCategory.product))
                .join(productCategory.category, category)
                .where(
                        product.market.id.eq(marketId),
                        category.id.eq(subCategoryId)
                )
                .fetchOne();
    }


    private List<ProductEntity> findProductsByMarketAndSubCategories(Long marketId, List<Long> subCategoryIds, Pageable pageable) {
        QProductEntity product = QProductEntity.productEntity;
        QProductCategoryEntity productCategory = QProductCategoryEntity.productCategoryEntity;
        QCategoryEntity category = QCategoryEntity.categoryEntity;

        return queryFactory
                .selectDistinct(product)
                .from(product)
                .join(productCategory).on(product.eq(productCategory.product))
                .join(productCategory.category, category)
                .where(
                        product.market.id.eq(marketId),
                        category.id.in(subCategoryIds)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private Long countProductsByMarketAndSubCategories(Long marketId, List<Long> subCategoryIds) {
        QProductEntity product = QProductEntity.productEntity;
        QProductCategoryEntity productCategory = QProductCategoryEntity.productCategoryEntity;
        QCategoryEntity category = QCategoryEntity.categoryEntity;

        return queryFactory
                .select(product.countDistinct())
                .from(product)
                .join(productCategory).on(product.eq(productCategory.product))
                .join(productCategory.category, category)
                .where(
                        product.market.id.eq(marketId),
                        category.id.in(subCategoryIds)
                )
                .fetchOne();
    }

    @Override
    public Long countProductsByFilter(FilterInfo filterInfo) {
        QProductEntity product = QProductEntity.productEntity;
        QProductCategoryEntity productCategory = QProductCategoryEntity.productCategoryEntity;
        QCategoryEntity category = QCategoryEntity.categoryEntity;

        BooleanBuilder builder = buildProductConditions(filterInfo, product);

        JPAQuery<Long> query = queryFactory
                .select(product.id.countDistinct())
                .from(product)
                .join(productCategory).on(product.id.eq(productCategory.product.id))
                .join(category).on(productCategory.category.id.eq(category.id))
                .where(builder)
                .where(buildCategoryCondition(filterInfo, category));

        return query.fetchOne();
    }

    private BooleanBuilder buildProductConditions(FilterInfo filterInfo, QProductEntity product) {
        BooleanBuilder builder = new BooleanBuilder();

        filterInfo.startPrice().ifPresent(start -> builder.and(product.price.goe(start)));
        filterInfo.endPrice().ifPresent(end -> builder.and(product.price.loe(end)));

        // 사용 예정 : packingType 및 coupon 도메인 추가 필요

        if (filterInfo.packagingTypes() != null && !filterInfo.packagingTypes().isEmpty()) {
            builder.and(product.packagingType.in(filterInfo.packagingTypes()));
        }

        /*
        if (filterInfo.benefitTypes() != null && !filterInfo.benefitTypes().isEmpty()) {
            builder.and(product.benefit.in(filterInfo.benefitTypes()));
        }
        */

        return builder;
    }

    private BooleanBuilder buildCategoryCondition(FilterInfo filterInfo, QCategoryEntity category) {
        BooleanBuilder builder = new BooleanBuilder();

        if (filterInfo.categoryIds() != null && !filterInfo.categoryIds().isEmpty()) {
            List<Long> resolvedIds = resolveCategoryIds(filterInfo.categoryIds());

            if (!resolvedIds.isEmpty()) {
                builder.and(category.id.in(resolvedIds));
            }
        }

        return builder;
    }

    private List<Long> resolveCategoryIds(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }

        List<Long> allSubCategoryIds = new ArrayList<>();

        for (Long id : categoryIds) {
            List<Long> subIds = findSubCategoryIdsByMainCategory(id);

            if (subIds.isEmpty()) {
                allSubCategoryIds.add(id);
            } else {
                allSubCategoryIds.addAll(subIds);
            }
        }

        return allSubCategoryIds;
    }

    @Override
    public Page<ProductEntity> findFilteredProducts(FilterInfo filterInfo, Pageable pageable,Optional<SortType> sortType) {
        List<ProductEntity> content = fetchFilteredProducts(filterInfo, pageable, sortType);
        Long total = fetchFilteredProductsCount(filterInfo);

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private List<ProductEntity> fetchFilteredProducts(FilterInfo filterInfo, Pageable pageable, Optional<SortType> sortType) {
        QProductEntity product = QProductEntity.productEntity;
        QProductCategoryEntity productCategory = QProductCategoryEntity.productCategoryEntity;
        QCategoryEntity category = QCategoryEntity.categoryEntity;

        BooleanBuilder builder = buildProductConditions(filterInfo, product);

        JPAQuery<ProductEntity> query = queryFactory
                .select(product)
                .from(product)
                .join(productCategory).on(product.id.eq(productCategory.product.id))
                .join(category).on(productCategory.category.id.eq(category.id))
                .where(builder)
                .where(buildCategoryCondition(filterInfo, category))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        applySort(query, sortType, product);

        return query.fetch();
    }

    private Long fetchFilteredProductsCount(FilterInfo filterInfo) {
        QProductEntity product = QProductEntity.productEntity;
        QProductCategoryEntity productCategory = QProductCategoryEntity.productCategoryEntity;
        QCategoryEntity category = QCategoryEntity.categoryEntity;

        BooleanBuilder builder = buildProductConditions(filterInfo, product);

        return queryFactory
                .select(product.id.countDistinct())
                .from(product)
                .join(productCategory).on(product.id.eq(productCategory.product.id))
                .join(category).on(productCategory.category.id.eq(category.id))
                .where(builder)
                .where(buildCategoryCondition(filterInfo, category))
                .fetchOne();
    }

    //상품별 판매량 및 리뷰수 구현 필요
    private void applySort(JPAQuery<ProductEntity> query, Optional<SortType> sortType, QProductEntity product) {
        if (sortType.isEmpty()) {
            return;
        }

        switch (sortType.get()) {
            case DISCOUNT -> query.orderBy(product.discountRate.desc());
            //case REVIEW -> query.orderBy(product.market.totalReviews.desc());
            //case SALES -> query.orderBy(product.market.totalSales.desc());
            case PRICE_ASC -> query.orderBy(product.price.asc());
            case PRICE_DESC -> query.orderBy(product.price.desc());
        }
    }


}

