package com.fondant.wishlist.application.dto.request;

public record WishListRegistRequest(
        Long userId,
        Long productId
) {
}
