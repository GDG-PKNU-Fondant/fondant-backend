package com.fondant.coupon.domain.entity;

public enum CouponType {
    ALL_MARKET("전체 마켓"),
    MARKET_SPECIFIC("특정 마켓");

    private final String description;

    CouponType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}