package com.fondant.product.domain.repository;

import com.fondant.global.config.PageConfig;
import com.fondant.market.domain.entity.QMarketEntity;
import com.fondant.product.category.domain.QCategoryEntity;
import com.fondant.product.domain.entity.ProductEntity;
import com.fondant.product.domain.entity.QProductCategoryEntity;
import com.fondant.product.domain.entity.QProductEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

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
}

