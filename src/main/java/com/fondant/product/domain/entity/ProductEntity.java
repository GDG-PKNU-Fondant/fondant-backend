package com.fondant.product.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="product_id")
    private Long id;

    @Column(name="name",nullable = false)
    private String name;

    @Column(name="description",nullable = false)
    private String description;

    @Column(name="thumbnail",nullable = false)
    private String thumbnail;

    @Column(name="price",nullable = false)
    private String price;

    @Column(name="market_id",nullable = false)
    private Long marketId;

    @Column(name="start_date",nullable = false)
    private LocalDate startDate;

    @Column(name="maxCount",nullable = false)
    private int maxCount;

    @Column(name="discount rate",nullable = false)
    private double discountRate;

    @Builder
    public ProductEntity(String name, String description, String thumbnail, String price, Long marketId, LocalDate startDate, int maxCount) {
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
        this.price = price;
        this.marketId = marketId;
        this.startDate = startDate;
        this.maxCount = maxCount;
        this.discountRate = 0.0;
    }
}
