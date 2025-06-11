package com.fondant.coupon.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long id;

    @NotNull
    private String name;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @NotNull
    @Column(name = "discount_amount")
    private Double discountAmount;

    @NotNull
    @Column(name = "min_order_amount")
    private Double minOrderAmount;

    @NotNull
    @Column(name = "start_date")
    private LocalDateTime startDate;

    @NotNull
    @Column(name = "end_date")
    private LocalDateTime endDate;

    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CouponMarketEntity> couponMarkets = new ArrayList<>();

    @Builder
    public CouponEntity(String name, DiscountType discountType, Double discountAmount,
                        Double minOrderAmount, LocalDateTime startDate, LocalDateTime endDate) {
        this.name = name;
        this.discountType = discountType;
        this.discountAmount = discountAmount;
        this.minOrderAmount = minOrderAmount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public boolean isGlobal() {
        return couponMarkets.isEmpty();
    }

    public boolean isAvailable() {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }
}