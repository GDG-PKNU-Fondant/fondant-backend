package com.fondant.wishlist.presentation;

import com.fondant.global.dto.PageInfo;
import com.fondant.product.application.ProductService;
import com.fondant.product.application.dto.ProductInfo;
import com.fondant.product.domain.entity.ProductEntity;
import com.fondant.product.presentation.dto.response.ProductsResponse;
import com.fondant.wishlist.domain.entity.WishListEntity;
import com.fondant.wishlist.domain.repository.WishListRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishListService {
    private final WishListRepository wishListRepository;
    private final ProductService productService;

    public WishListService(WishListRepository wishListRepository, ProductService productService) {
        this.wishListRepository = wishListRepository;
        this.productService = productService;
    }

    @Transactional
    public void registerWishList(Long userId, Long productId) {
        wishListRepository.save(
                WishListEntity.builder()
                        .userId(userId)
                        .product(productService.getProductById(productId))
                        .build()
        );
    }

    @Transactional
    public ProductsResponse getWishList(Long userId, Pageable pageable) {
        Page<WishListEntity> wishlist = getWishListsByUser(userId,pageable);
        List<ProductInfo> products = productService.getProductInfos(
                getProducts(wishlist.getContent()));

        return ProductsResponse.of(products,new PageInfo(pageable.getPageNumber(),pageable.getPageSize()));
    }

    public Page<WishListEntity> getWishListsByUser(Long userId,Pageable pageable) {
        return wishListRepository.findByUserId(userId,pageable);
    }

    public List<ProductEntity> getProducts(List<WishListEntity> wishListEntities){
        return wishListEntities.stream().map(WishListEntity::getProduct).toList();
    }
}
