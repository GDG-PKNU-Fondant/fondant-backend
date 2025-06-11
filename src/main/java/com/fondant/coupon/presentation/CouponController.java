package com.fondant.coupon.presentation;

import com.fondant.coupon.application.CouponService;
import com.fondant.coupon.presentation.dto.request.CouponIssueRequest;
import com.fondant.coupon.presentation.dto.response.CouponListResponse;
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
@RequestMapping("/api/coupon")
public class CouponController {
    private final CouponService couponService;

    @PostMapping("/issue")
    public ResponseEntity<ResponseDto<Void>> issueCoupon(
            @CurrentUser CustomUserDetails user,
            @RequestBody CouponIssueRequest request) {

        couponService.issueCoupon(user.getUserId(), request.couponId());
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }

    @GetMapping("/issued")
    public ResponseEntity<ResponseDto<CouponListResponse>> getIssuedCoupons(
            @CurrentUser CustomUserDetails user,
            @PageableDefault Pageable pageable
    ) {
        CouponListResponse slice = couponService.getIssuedCoupons(user.getUserId(), pageable);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, slice));
    }

    @GetMapping("/issuable")
    public ResponseEntity<ResponseDto<CouponListResponse>> getIssuableCoupons(
            @CurrentUser CustomUserDetails user,
            @PageableDefault Pageable pageable
    ) {
        CouponListResponse slice = couponService.getIssuableCoupons(user.getUserId(), pageable);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, slice));
    }
}
