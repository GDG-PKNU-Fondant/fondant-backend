package com.fondant.product.domain.repository;

import com.fondant.market.domain.entity.QMarketEntity;
import com.fondant.product.domain.entity.ProductEntity;
import com.fondant.product.domain.entity.QCategoryEntity;
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
    public Page<ProductEntity> findProductsByMarketAndCategory(Long marketId, Long categoryId, Pageable pageable) {
        QProductCategoryEntity productCategory = QProductCategoryEntity.productCategoryEntity;
        QProductEntity product = QProductEntity.productEntity;
        QMarketEntity market = QMarketEntity.marketEntity;

        List<ProductEntity> content = queryFactory
                .select(product)
                .from(productCategory)
                .join(productCategory.product, product)
                .join(product.market, market)
                .where(
                        productCategory.category.id.eq(categoryId),
                        product.market.id.eq(marketId)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(product.count())
                .from(productCategory)
                .join(productCategory.product, product)
                .join(product.market, market)
                .where(
                        productCategory.category.id.eq(categoryId),
                        product.market.id.eq(marketId)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}

