package com.fondant.coupon.domain.entity;

import com.fondant.market.domain.entity.MarketEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "coupon_market")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponMarketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_market_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private CouponEntity coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_id")
    private MarketEntity market;

    @Builder
    public CouponMarketEntity(CouponEntity coupon, MarketEntity market) {
        this.coupon = coupon;
        this.market = market;
    }

}