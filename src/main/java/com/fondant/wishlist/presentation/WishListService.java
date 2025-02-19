package com.fondant.wishlist.presentation;

import com.fondant.product.application.ProductService;
import com.fondant.wishlist.domain.entity.WishListEntity;
import com.fondant.wishlist.domain.repository.WishListRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

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
}
