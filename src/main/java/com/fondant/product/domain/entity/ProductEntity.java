package com.fondant.product.domain.entity;

import com.fondant.market.domain.entity.MarketEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="product")
@Getter
public class ProductEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="product_id")
    private Long id;

    @NotNull
    @Column(name="name",nullable = false)
    private String name;

    @NotNull
    @Column(name="description")
    private String description;

    @NotNull
    @Column(name="thumbnail")
    private String thumbnail;

    @NotNull
    @Column(name="price")
    private Double price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="market_id")
    private MarketEntity market;

    @NotNull
    @Column(name="start_date")
    private LocalDate startDate;

    @NotNull
    @Column(name="max_count")
    private int maxCount;

    @NotNull
    @Column(name="discount_rate")
    private Double discountRate = 0.0;

    @NotNull
    @Column(name="packaging_type")
    private PackagingType packagingType;

    @NotNull
    @Column(name="total_reviews")
    private int totalReviews = 0;

    @Builder
    public ProductEntity(PackagingType packagingType, String name, String description, String thumbnail, Double price, MarketEntity market, LocalDate startDate, int maxCount) {
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
        this.price = price;
        this.market = market;
        this.startDate = startDate;
        this.maxCount = maxCount;
        this.packagingType = packagingType;
    }

    public void updateDiscountRate(double discountRate) {
        this.discountRate = discountRate;
    }

    public void incrementReviewCount() {
        this.totalReviews += 1;
    }
}
