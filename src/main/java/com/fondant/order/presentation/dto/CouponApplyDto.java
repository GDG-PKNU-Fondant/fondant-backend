package com.fondant.order.presentation.dto;

public record CouponApplyDto(
        Long couponId,
        Long targetItemId
) {}