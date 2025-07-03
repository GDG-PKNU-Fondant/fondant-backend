package com.fondant.cart.presentation;

import com.fondant.cart.application.CartService;
import com.fondant.cart.application.dto.CartInfo;
import com.fondant.cart.application.dto.CartUpdateInfo;
import com.fondant.cart.presentation.dto.response.CartResponse;
import com.fondant.global.annotation.CurrentUser;
import com.fondant.global.dto.ResponseDto;
import com.fondant.global.dto.SuccessMessage;
import com.fondant.user.application.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @GetMapping("/list")
    public ResponseEntity<ResponseDto<CartResponse>> getCartItems(
            @CurrentUser CustomUserDetails user,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        CartResponse response = cartService.getCartItemsByUser(user.getUserId(), pageable);
        return ResponseEntity.ok(
                ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, response)
        );
    }

    @PostMapping("/add")
    public ResponseEntity<ResponseDto<Void>> addCartItem(
            @CurrentUser CustomUserDetails user,
            @RequestBody CartInfo request
    ) {
        cartService.addCartItem(user.getUserId(), request);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }

}