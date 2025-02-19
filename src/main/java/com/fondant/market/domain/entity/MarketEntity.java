package com.fondant.market.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="market")
public class MarketEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="market_id")
    private Long id;

    @NotNull
    @Column(name="market_name", nullable = false)
    private String name;

    @NotNull
    @Column(name="total_sales", nullable = false)
    private Long totalSales;

    @NotNull
    @Column(name="total_reviews", nullable = false)
    private Long totalReviews;

    @NotNull
    @Column(name="create_at", nullable = false)
    private LocalDate createAt;

    @Column(name="update_at")
    private LocalDate updateAt;

    @Column(name="description")
    private String description;

    @NotNull
    @Column(name="thumbnail")
    private String thumbnail;

    @NotNull
    @Column(name="background")
    private String background;

    @NotNull
    @Column(name="delivery_fee", nullable = false)
    private long deliveryFee;

    @Builder MarketEntity(String name, Long totalSales, Long totalReviews, LocalDate createAt, LocalDate updateAt, String description, String thumbnail, String background, long deliveryFee) {
        this.name = name;
        this.totalSales = 0L;
        this.totalReviews = 0L;
        this.createAt = LocalDate.now();
        this.updateAt = updateAt;
        this.description = description;
        this.thumbnail = thumbnail;
        this.background = background;
        this.deliveryFee = 0L;
    }
}