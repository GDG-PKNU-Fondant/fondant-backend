package com.fondant.coupon.domain.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("GLOBAL")
public class GlobalCouponEntity extends CouponEntity {

    @Builder
    public GlobalCouponEntity(String name, Integer discountAmount, Integer minOrderAmount,
                              LocalDateTime startDate, LocalDateTime endDate) {
        super(name, discountAmount, minOrderAmount, startDate, endDate);
    }

    @Override
    public boolean isAvailableForMarket(Long marketId) {
        return true;
    }
}