package com.fondant.product.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="product")
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
    private String price;

    @NotNull
    @Column(name="market_id")
    private Long marketId;

    @NotNull
    @Column(name="start_date")
    private LocalDate startDate;

    @NotNull
    @Column(name="maxCount")
    private int maxCount;

    @NotNull
    @Column(name="discount rate")
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
