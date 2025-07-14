package com.fondant.coupon.presentation.dto.response;

import com.fondant.coupon.application.dto.CouponInfo;
import com.fondant.global.dto.SliceInfo;

import java.util.List;

public record CouponListResponse(
        SliceInfo sliceInfo,
        List<CouponInfo> coupons
) {
    public static CouponListResponse of(List<CouponInfo> coupons, SliceInfo sliceInfo) {
        return new CouponListResponse(sliceInfo, coupons);
    }
}