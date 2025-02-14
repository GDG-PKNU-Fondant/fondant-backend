package com.fondant.product.application;

import com.fondant.global.dto.PageInfo;
import com.fondant.global.exception.ApiException;
import com.fondant.product.application.dto.OptionInfo;
import com.fondant.product.application.dto.ProductInfo;
import com.fondant.product.domain.entity.ImageType;
import com.fondant.product.domain.entity.OptionEntity;
import com.fondant.product.domain.entity.ProductEntity;
import com.fondant.product.domain.entity.ProductImageEntity;
import com.fondant.product.domain.repository.OptionRepository;
import com.fondant.product.domain.repository.ProductImageRepository;
import com.fondant.product.domain.repository.ProductRepository;
import com.fondant.product.exception.ProductError;
import com.fondant.product.presentation.dto.response.ProductDetailResponse;
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
    private final ProductImageRepository productImageRepository;
    private final OptionRepository optionRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, ProductImageRepository productImageRepository, OptionRepository optionRepository) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.optionRepository = optionRepository;
    }

    @Transactional(readOnly = true)
    public ProductsResponse getProductsByMarketAndCategoryId(Long marketId, Long categoryId, Pageable pageable) {
        Page<ProductEntity> products = productRepository.findProductsByMarketAndCategory(marketId,categoryId,pageable);

        if(products.isEmpty()){
            throw new ApiException(ProductError.NO_PRODUCTS_FOUND);
        }

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

    public ProductDetailResponse getProductDetail(Long productId) {
        ProductEntity product = getProductById(productId);
        return ProductDetailResponse.builder()
                .photos(getImageUrlsByProductIdAndType(productId,ImageType.PRODUCT_PHOTO))
                .name(product.getName())
                .options(getOptionInfos(productId))
                .description(product.getDescription())
                .detailPages(getImageUrlsByProductIdAndType(productId,ImageType.DETAIL_PAGE))
                .build();
    }

    private List<String> getImageUrlsByProductIdAndType(Long productId,ImageType imageType){
        return productImageRepository.findByProductIdAndImageType(productId, imageType)
                .stream().map(ProductImageEntity::getImageUrl).toList();
    }

    private List<OptionInfo> getOptionInfos(Long productId){
         List<OptionEntity> options = optionRepository.findByProductId(productId);

        if (options.isEmpty()) {
            throw new ApiException(ProductError.OPTION_NOT_FOUND);
        }

         return options.stream().map(option->
                 new OptionInfo(option.getId(),option.getName(),option.getPrice()))
                 .toList();
    }

    private ProductEntity getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(ProductError.PRODUCT_NOT_FOUND));
    }
}