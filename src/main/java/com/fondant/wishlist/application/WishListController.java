package com.fondant.wishlist.application;

import com.fondant.global.dto.ResponseDto;
import com.fondant.global.dto.SuccessMessage;
import com.fondant.wishlist.presentation.WishListService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishlist")
public class WishListController {
    private final WishListService wishListService;

    public WishListController(WishListService wishListService) {
        this.wishListService = wishListService;
    }

    @PostMapping("")
    public ResponseDto<Void> registerWishlist(
            @RequestParam Long userId,/*토큰(로그인) 로직 완료 시 토큰으로 받도록 수정 예정*/
            @RequestParam Long productId
    ){
        wishListService.registerWishList(userId, productId);
        return ResponseDto.ofSuccess(SuccessMessage.CREATE_SUCCESS);
    }
}
