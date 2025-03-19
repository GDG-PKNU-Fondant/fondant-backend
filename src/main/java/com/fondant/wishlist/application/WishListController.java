package com.fondant.wishlist.application;

import com.fondant.global.annotation.CurrentUser;
import com.fondant.global.dto.ResponseDto;
import com.fondant.global.dto.SuccessMessage;
import com.fondant.product.presentation.dto.response.ProductsResponse;
import com.fondant.user.application.dto.CustomUserDetails;
import com.fondant.wishlist.application.dto.request.WishListRegistRequest;
import com.fondant.wishlist.presentation.WishListService;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
public class WishListController {
    private static int PAGE_SIZE = 10;

    private final WishListService wishListService;

    public WishListController(WishListService wishListService) {
        this.wishListService = wishListService;
    }

    @PostMapping("")
    public ResponseEntity<ResponseDto<Void>> registerWishlist(
            @CurrentUser CustomUserDetails user,
            @RequestBody WishListRegistRequest request
            ){
        wishListService.registerWishList(user.getUserId(), request.productId());
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.CREATE_SUCCESS));
    }

    @GetMapping("")
    public ResponseEntity<ResponseDto<ProductsResponse>> getWishLists(
            @CurrentUser CustomUserDetails user,
            @RequestParam(name="page") int page
    ){
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                wishListService.getWishList(user.getUserId(),pageable)));
    }
}
