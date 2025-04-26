package com.fondant.product.domain.repository;

import com.fondant.product.application.dto.FilterInfo;
import com.fondant.product.category.domain.QCategoryEntity;
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

public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public ProductRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<ProductEntity> findProductsByMarketAndCategory(Long marketId, Long mainCategoryId, Pageable pageable) {
        List<Long> subCategoryIds = findSubCategoryIdsByMainCategory(mainCategoryId);

        if (subCategoryIds.isEmpty()) {
            return Page.empty(pageable);
        }

        List<ProductEntity> content = findProductsByMarketAndSubCategories(marketId, subCategoryIds, pageable);
        Long total = countProductsByMarketAndSubCategories(marketId, subCategoryIds);

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
        /*
        if (filterInfo.packingTypes() != null && !filterInfo.packingTypes().isEmpty()) {
            builder.and(product.packagingType.in(filterInfo.packingTypes()));
        }

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
    public Page<ProductEntity> findFilteredProducts(FilterInfo filterInfo, Pageable pageable) {
        List<ProductEntity> content = fetchFilteredProducts(filterInfo, pageable);
        Long total = fetchFilteredProductsCount(filterInfo);

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private List<ProductEntity> fetchFilteredProducts(FilterInfo filterInfo, Pageable pageable) {
        QProductEntity product = QProductEntity.productEntity;
        QProductCategoryEntity productCategory = QProductCategoryEntity.productCategoryEntity;
        QCategoryEntity category = QCategoryEntity.categoryEntity;

        BooleanBuilder builder = buildProductConditions(filterInfo, product);

        return queryFactory
                .select(product)
                .from(product)
                .join(productCategory).on(product.id.eq(productCategory.product.id))
                .join(category).on(productCategory.category.id.eq(category.id))
                .where(builder)
                .where(buildCategoryCondition(filterInfo, category))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
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
}

