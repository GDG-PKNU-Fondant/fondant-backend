package com.fondant.coupon.domain.entity;

import com.fondant.market.domain.entity.MarketEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("MARKET")
public class MarketCouponEntity extends CouponEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_id")
    private MarketEntity market;

    @Builder
    public MarketCouponEntity(String name, Integer discountAmount, Integer minOrderAmount,
                              LocalDateTime startDate, LocalDateTime endDate, MarketEntity market) {
        super(name, discountAmount, minOrderAmount, startDate, endDate);
        this.market = market;
    }

    @Override
    public boolean isAvailableForMarket(Long marketId) {
        return this.market.getId().equals(marketId);
    }
}