package com.fondant.product.application;

import com.fondant.global.dto.PageInfo;
import com.fondant.product.application.dto.ProductInfo;
import com.fondant.product.domain.entity.ProductEntity;
import com.fondant.product.domain.repository.ProductRepository;
import com.fondant.product.presentation.dto.response.ProductsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public ProductsResponse getProductsByMarketAndCategoryId(Long marketId, Long categoryId, Pageable pageable) {
        Page<ProductEntity> products = productRepository.findProductsByMarketAndCategory(marketId,categoryId,pageable);

        return ProductsResponse.builder()
                .pageInfo(PageInfo.of(products.getNumber(), products.getTotalPages()))
                .products(getProductInfos(products.getContent()))
                .build();
    }

    private List<ProductInfo> getProductInfos(List<ProductEntity> products) {
        return products.stream()
                .map(product->
                        ProductInfo.builder()
                                .id(product.getId())
                                .name(product.getName())
                                .price(product.getPrice())
                                .thumbnailUrl(product.getThumbnail())
                                .discountRate(product.getDiscountRate())
                                .discountPrice(getDiscountedPrice(product.getPrice(),product.getDiscountRate()))
                                .build()
                ).toList();
    }

    private int getDiscountedPrice(int price, double discountRate) {
        double appliedRate = 1.0 - discountRate;
        return (int) Math.floor(price * appliedRate + 0.5);
    }
}
