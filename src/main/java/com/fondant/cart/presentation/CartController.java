package com.fondant.cart.presentation;

import com.fondant.cart.application.CartService;
import com.fondant.cart.presentation.dto.response.CartResponse;
import com.fondant.global.annotation.CurrentUser;
import com.fondant.global.config.PageConfig;
import com.fondant.global.dto.ResponseDto;
import com.fondant.global.dto.SuccessMessage;
import com.fondant.user.application.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final PageConfig pageConfig;

    @GetMapping
    public ResponseEntity<ResponseDto<CartResponse>> getCartItems(
            @CurrentUser CustomUserDetails user,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                        cartService.getCartItemsByUser(user.getUserId(), pageable))
        );
    }
}