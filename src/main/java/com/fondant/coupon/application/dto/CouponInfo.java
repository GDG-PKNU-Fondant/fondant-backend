package com.fondant.coupon.application.dto;

import com.fondant.coupon.domain.entity.CouponEntity;
import com.fondant.coupon.domain.entity.DiscountType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record CouponInfo(
        Long id,
        String name,
        DiscountType discountType,
        Double discountAmount,
        Double minOrderAmount,
        LocalDateTime startDate,
        LocalDateTime endDate,
        boolean isGlobal,
        List<MarketInfo> markets
) {
    public record MarketInfo(
            Long id,
            String name
    ) {}

    public static CouponInfo from(CouponEntity coupon) {
        return new CouponInfo(
                coupon.getId(),
                coupon.getName(),
                coupon.getDiscountType(),
                coupon.getDiscountAmount(),
                coupon.getMinOrderAmount(),
                coupon.getStartDate(),
                coupon.getEndDate(),
                coupon.isGlobal(),
                coupon.getCouponMarkets().stream()
                        .map(m -> new MarketInfo(
                                m.getMarket().getId(),
                                m.getMarket().getName()
                        ))
                        .collect(Collectors.toList())
        );
    }
}