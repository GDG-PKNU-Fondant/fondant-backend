package com.fondant.product.application;

import com.fondant.global.dto.PageInfo;
import com.fondant.global.exception.ApiException;
import com.fondant.market.application.MarketService;
import com.fondant.market.application.dto.MarketInfoForProductDetail;
import com.fondant.market.domain.entity.MarketEntity;
import com.fondant.product.application.dto.*;
import com.fondant.product.category.application.CategoryService;
import com.fondant.product.domain.entity.ImageType;
import com.fondant.product.domain.entity.OptionEntity;
import com.fondant.product.domain.entity.ProductEntity;
import com.fondant.product.domain.repository.OptionRepository;
import com.fondant.product.domain.repository.ProductImageRepository;
import com.fondant.product.domain.repository.ProductRepository;
import com.fondant.product.exception.ProductError;
import com.fondant.product.presentation.dto.response.FilteredProductCountResponse;
import com.fondant.product.presentation.dto.response.ProductDetailResponse;
import com.fondant.product.presentation.dto.response.ProductsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final CategoryService categoryService;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final OptionRepository optionRepository;
    private final MarketService marketService;

    @Autowired
    public ProductService(CategoryService categoryService, ProductRepository productRepository, ProductImageRepository productImageRepository, OptionRepository optionRepository,MarketService marketService) {
        this.categoryService = categoryService;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.optionRepository = optionRepository;
        this.marketService = marketService;
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

    public List<ProductInfo> getProductInfos(List<ProductEntity> products) {
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

    private Double getDiscountedPrice(Double price, Double discountRate) {
        Double appliedRate = 1.0 - discountRate;
        return Math.floor(price * appliedRate + 0.5);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetail(Long productId) {
        ProductEntity product = getProductById(productId);

        return ProductDetailResponse.builder()
                .photos(getImageUrlsByProductIdAndType(productId,ImageType.PRODUCT_PHOTO))
                .name(product.getName())
                .options(getOptionInfos(productId))
                .description(product.getDescription())
                .detailPages(getImageUrlsByProductIdAndType(productId,ImageType.DETAIL_PAGE))
                .marketInfo(getMarketInfo(product.getMarket()))
                .basePrice(product.getPrice())
                .build();
    }

    private MarketInfoForProductDetail getMarketInfo(MarketEntity market) {
        return MarketInfoForProductDetail.builder()
                .id(market.getId())
                .name(market.getName())
                .description(market.getDescription())
                .thumbnail(market.getThumbnail())
                .totalReviews(market.getTotalReviews())
                .totalSales(market.getTotalSales())
                .freeDeliveryLimit(market.getFreeDeliveryLimit())
                .build();
    }
    private List<ImageInfo> getImageUrlsByProductIdAndType(Long productId, ImageType imageType){
        return productImageRepository.findByProductIdAndImageTypeOrderByImgOrderAsc(productId, imageType)
                .stream().map(img->
                        new ImageInfo(img.getImageUrl(),img.getImgOrder())
                ).toList();
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

    public ProductEntity getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(ProductError.PRODUCT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public FilteredProductCountResponse getFilteredProductCounts(FilterInfo filterInfo) {
        Long count = productRepository.countProductsByFilter(filterInfo);
        return new FilteredProductCountResponse(count);
    }

    @Transactional(readOnly = true)
    public ProductsResponse getFilteredProducts(FilterInfo filterInfo, Pageable pageable, Optional<SortType> sortType) {
        Page<ProductEntity> products = productRepository.findFilteredProducts(filterInfo,pageable,sortType);

        if(products.isEmpty()){
            throw new ApiException(ProductError.NO_PRODUCTS_FOUND);
        }

        return ProductsResponse.builder()
                .pageInfo(PageInfo.of(products.getNumber(), products.getTotalPages()))
                .products(getProductInfos(products.getContent()))
                .build();
    }
}
